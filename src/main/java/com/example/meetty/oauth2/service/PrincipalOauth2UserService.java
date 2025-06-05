package com.example.meetty.oauth2.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.entity.UserRole;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.config.auth.CustomUserDetails;
import com.example.meetty.image.service.UserImageService;
import com.example.meetty.oauth2.info.GoogleUserInfo;
import com.example.meetty.oauth2.info.KakaoUserInfo;
import com.example.meetty.oauth2.info.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserImageService userImageService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;

        String provider = userRequest.getClientRegistration().getRegistrationId();

        if (provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
        } else if (provider.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oauth2User.getAttributes());
        }

        log.info("OAuth2 User Attributes: {}", oauth2User.getAttributes());

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();

        // ✅ 랜덤 이메일 생성 조건
        if (email == null || userRepository.existsByEmail(email)) {
            email = "oauth_" + UUID.randomUUID().toString().substring(0, 8) + "@oauthuser.com";
        }

        // ✅ provider + providerId 로 유저 조회 (OAuth2 기준)
        Optional<UserEntity> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        UserEntity oauthUser;

        if (existingUser.isPresent()) {
            oauthUser = existingUser.get();
        } else {
            // 유저네임 중복 방지
            String baseUsername = oAuth2UserInfo.getName();
            String finalUsername = baseUsername;
            if (userRepository.findByUsername(finalUsername).isPresent()) {
                finalUsername = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 8);
            }

            oauthUser = UserEntity.builder()
                    .email(email)
                    .password("OAuth2")
                    .username(finalUsername)
                    .provider(provider)
                    .providerId(providerId)
                    .role(UserRole.ROLE_USER)
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            UserEntity savedUser = userRepository.save(oauthUser);
            UserEntity managedUser = userRepository.findByUserId(savedUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("유저 저장 실패 후 조회 불가"));

            try {
                String imageUrl = oAuth2UserInfo.getImage();
                if (imageUrl == null || imageUrl.isBlank()) {
                    userImageService.uploadUserImage(managedUser, null, true);
                } else {
                    userImageService.uploadUserImageFromUrl(managedUser, imageUrl);
                }
            } catch (Exception e) {
                log.error("프로필 이미지 저장 실패", e);
            }
        }

        return new CustomUserDetails(oauthUser, oauth2User.getAttributes());
    }
}
