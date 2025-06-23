package me.notej.notej_api.security.oauth2.user;

import lombok.RequiredArgsConstructor;
import me.notej.notej_api.security.oauth2.exception.OAuth2AuthenticationProcessingException;
import me.notej.notej_api.security.oauth2.user.google.GoogleOAuth2UserUnlink;
import me.notej.notej_api.security.oauth2.user.naver.NaverOAuth2UserUnlink;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OAuth2UserUnlinkManager {

    private final GoogleOAuth2UserUnlink googleOAuth2UserUnlink;
    private final NaverOAuth2UserUnlink naverOAuth2UserUnlink;

    public void unlink(OAuth2Provider provider, String accessToken) {
        if (OAuth2Provider.GOOGLE.equals(provider)) {
            googleOAuth2UserUnlink.unlink(accessToken);
        } else if (OAuth2Provider.NAVER.equals(provider)) {
            naverOAuth2UserUnlink.unlink(accessToken);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Unlink with " + provider.getRegistrationId() + " is not supported");
        }
    }
}
