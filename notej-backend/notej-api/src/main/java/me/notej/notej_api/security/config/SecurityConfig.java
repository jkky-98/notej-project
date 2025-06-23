package me.notej.notej_api.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.notej.notej_api.security.oauth2.handler.HttpCookieOAuth2AuthorizationRequestRepository;
import me.notej.notej_api.security.credentials.service.CredentialsUserDetailsService;
import me.notej.notej_api.security.oauth2.handler.OAuth2AuthenticationFailureHandler;
import me.notej.notej_api.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import me.notej.notej_api.security.oauth2.service.CustomOAuth2UserService;
import me.notej.notej_api.security.jwt.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final CredentialsUserDetailsService credentialsUserDetailsService;

    /**
     * 비밀번호 해싱
     * DB에는 해시값만 저장, 로그인 시 입력된 비밀번호를 해싱하여 저장된 해시와 비교
     * DetailService에 등록해서 사용
     * 추후 직접 로그인 기능에서 사용할 것
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(credentialsUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // CSRF 방어 기능 비활성화 (폼, 세션 로그인시 필요 -> 아마 필요없을듯!)
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 막아놓음.
                .httpBasic(AbstractHttpConfigurer::disable) // Authorization: Basic 인증 막음(JWT 기반이라)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/credentials/**", "/api/refresh").permitAll()
                        .requestMatchers("/login", "/error/**", "/logo/**", "/js/**", "/default-ui.css, /favicon.ico").permitAll()
                        .anyRequest().authenticated() // 나머지 경로는 모두 인가 필요
                )
                .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // HTTP 세션 생성 및 사용 하지 않음
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"Unauthorized - 인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"Forbidden - 권한이 없습니다.\"}");
                        })
                )
                /**
                 * oauth2Login가 삽입하는 필터들은 OAuth2AuthorizationRequestRedirectFilter, OAuth2LoginAuthenticationFilter들로 UsernamePasswordAuthenticationFilter보다 뒤에 위치함.(jwtAuthorizationFilter보다 무조건 뒤에서 실행되는 필터란 뜻)
                 * 인가 요청 단계 커스터마이징
                 * 만약 jwtAuthorizationFilter의 관문을 넘지 못해서 인증이 필요할 경우
                 * 아래 oauth2Login 로직을 타게 된다.
                 * 1. .loginPage("/login") :: 소셜로그인 페이지로 이동
                 * 2. 사용자가 네이버, 구글과 같은 버튼을 눌러 /oauth2/authorization/{registrationId} 경로에 접근 요청
                 * 3. OAuth2AuthorizationRequestRedirectFilter가 이를 가로채어 DefaultOAuth2AuthorizationRequestResolver로 하여금 OAuth2AuthorizationRequest를 생성
                 * 4. httpCookieOAuth2AuthorizationRequestRepository에 의해 응답 쿠키에 OAuth2AuthorizationRequest를 Base64로 직렬화하여 저장(여러번의 요청 응답 과정에서 mode와 같은 것들이 누락되지 않도록 잠깐 쿠키에 넣어놓고 이를 기억하기 위함.)
                 * 5. 302 리다이렉트로 하여금 https://accounts.google.com/o/oauth2/v2/auth?...&state=XYZ로 리다이렉트 시킴(이때부터 httpCookieOAuth2AuthorizationRequestRepository에 의한 쿠키가 유지되고 있음)
                 * 6. 사용자가 리다이렉트된 로그인 페이지에 해당 리소스 서비스 회사의 email or id, pw를 입력하여 로그인
                 * 7. 리소스 서버는 application.yml의 redirect-uri: 에 적힌 경로로 http://localhost:8080/login/oauth2/code/google?code=ABCD1234&state=XYZ5678 와 같이 302 응답 줌.
                 * 8. 브라우저는 http://localhost:8080/login/oauth2/code/google?code=ABCD1234&state=XYZ5678로 자동 요청되어 인증서버는 이를 다시 가로채어 쿼리 스트링에 있는 구글로부터의 CODE를 얻게 됨.
                 * 9. .oauth2Login을 사용하면 알아서 AntPathRequestMatcher("/login/oauth2/code/*")가 구축되는데 해당 경로로 오는 요청에 대해서는 httpCookieOAuth2AuthorizationRequestRepository의 removeAuthorizationRequest가 작동한다.
                 * 10. removeAuthorizationRequest가 작동하면 쿠키를 역직렬화한 OAuth2AuthorizationRequest을 얻게 된다.
                 * 11. CSRF 검증을 무시하지 않았다면 OAuth2AuthorizationRequest의 state와 구글로부터 넘어온 state의 일치를 비교하여 검증함.
                 * 12. code, state, requestUri로 하여금 OAuth2AuthorizationResponse를 만듬(이때 requestUri는 http://localhost:8080/login/oauth2/code/google?code=ABCD1234&state=XYZ5678가 된다.)
                 * 13. OAuth2AuthorizationRequest, OAuth2AuthorizationResponse로 하여금 OAuth2AuthorizationExchange를 만들어냄
                 * 14. OAuth2AuthorizationResponse로 하여금 리소스서버로부터 accesstoken을 받음
                 * 15. 받은 액세스 토큰으로 하여금 OAuth2UserRequest를 만들어 .userService(customOAuth2UserService)에 loadUser() 호출 [.userService(customOAuth2UserService) 시작]
                 * 16. 리소스 서버로부터 UserInfo를 받음 { "sub":"12345", "email":"user@example.com", "name":"홍길동", "picture":"..." } -> UserPrincipal로 만듬 [userService 종료]
                 * 17. 내부적으로 OAuth2LoginAuthenticationFilter이 Principal로 하여금 Authentication 객체를 만들어 [석세스 핸들러 시작] successHandler.onAuthenticationSuccess(request, response, auth); 시작
                 * 18. Authentication객체 내의 UserPrincipal 정보를 바탕으로 어플리케이션상의 login처리(DB저장, 토큰 발급) 등을 처리
                 * 19. 이 모든 과정 도중 예외 발생시 oAuth2AuthenticationFailureHandler가 실패 콜백을 처리함.
                 */
                .oauth2Login(configure ->
                                configure
                                        .loginPage("/login")
                                        // 1) 인가(Authorization) 요청 시점 설정
                                        .authorizationEndpoint(endpoint ->
                                                endpoint
                                                        // • authorizationRequestRepository:
                                                        //   인가 요청 시 만들어진 OAuth2AuthorizationRequest 객체를
                                                        //   어떻게/어디에 저장할지 결정합니다.
                                                        //   HttpCookieOAuth2AuthorizationRequestRepository 를 쓰면
                                                        //   세션 대신 쿠키에 담아 두었다가, 콜백 시 다시 꺼내 씁니다.
                                                        .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                                        )
                                        // 2) 사용자 정보(UserInfo) 조회 시점 설정
                                        .userInfoEndpoint(endpoint ->
                                                endpoint
                                                        // • userService:
                                                        //   구글·네이버 등 OAuth2 공급자로부터 받은 액세스 토큰을
                                                        //   이용해 추가 사용자 프로필(이메일, 이름 등)을 조회하고,
                                                        //   그것을 애플리케이션 내 User 객체로 매핑하는 역할을 담당합니다.
                                                        .userService(customOAuth2UserService)
                                        )
                                        // 3) 인증 성공 후 처리 핸들러
                                        .successHandler(oAuth2AuthenticationSuccessHandler)
                                        //    • 로그인(인가) 성공 시 호출됩니다.
                                        //    • 여기서 JWT 쿠키 발급, DB에 사용자 저장, 리다이렉트 URL 결정 등
                                        //      애플리케이션 특화 후속 작업을 수행할 수 있습니다.
                                        // 4) 인증 실패 후 처리 핸들러
                                        .failureHandler(oAuth2AuthenticationFailureHandler)
                        //    • 로그인 실패(사용자 거부, 에러 등) 시 호출됩니다.
                        //    • 에러 로깅, 에러 페이지 리다이렉트, 에러 응답 JSON 반환 등을 처리합니다.
                );

        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class); // UsernamePasswordAuthenticationFilter 바로 앞에 위치

        return http.build();

    }
}
