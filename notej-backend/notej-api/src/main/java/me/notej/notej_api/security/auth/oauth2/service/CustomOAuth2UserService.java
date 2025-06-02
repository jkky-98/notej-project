package me.notej.notej_api.security.auth.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.security.auth.oauth2.exception.OAuth2AuthenticationProcessingException;
import me.notej.notej_api.security.auth.oauth2.user.OAuth2UserInfo;
import me.notej.notej_api.security.auth.oauth2.user.OAuth2UserInfoFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
@Slf4j
/**
 * 리소스 서버로부터 UserInfo를 가져오기 위함
 * 리소스 서버로부터 받은 accessToken으로 하여금 OAuth2UserRequest를 받고  CustomOAuth2UserService.loadUser(OAuth2UserRequest request)를 수행하는 것
 * 리소스 서버로부터 UserInfo를 받아 OAuth2User객체를 생성 (객체에 포함되는 내용은 아래와 비슷)
 * {   구글의 경우
 *   "sub":      "1234567890",
 *   "email":    "user@example.com",
 *   "email_verified": true,
 *   "name":     "홍길동",
 *   "given_name": "동",
 *   "family_name":"홍",
 *   "picture":  "https://…/photo.jpg",
 *   "locale":   "ko"
 * }
 *
 */
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // 1) 어느 공급자(Google, Naver 등)로부터 온 요청
        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId();

        // 2) 공급자로 접근했던 액세스 토큰 (필요시 추가 API 호출에 사용할 수도 있음)
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // 3) 실제 사용자 정보(attributes)를 표준화된 커스텀 OAuth2UserInfo DTO로 변환
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
                accessToken,
                oAuth2User.getAttributes());

        // 4) 애플리케이션 요구사항에 따른 검증/예외 처리 (이메일이 없으면 우리 서비스에서 사용할 수 없으니 실패 처리)
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        // 5) 검증을 통과한 정보를 바탕으로
        //    우리 애플리케이션의 Principal 객체(여기서는 OAuth2UserPrincipal) 를 생성해 반환
        //    Principal은 OAuth2User의 자식으로 "인증된 OAuth2User"라는 뜻으로 이해, 즉 인증된 유저 객체라는 시큐리티만의 검증된 객체란 뜻 -> Principal이 있어야 Authentication 객체 생성 가능
        return new OAuth2UserPrincipal(oAuth2UserInfo);
    }
}

