package com.server.money_touch.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class KakaoDTO {

    @Getter @Setter @NoArgsConstructor
    public static class OAuthToken {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter @Setter @NoArgsConstructor
    public static class KakaoProfile {
        private Long id;

        @JsonProperty("connected_at")
        private String connectedAt;

        private Properties properties;

        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;

        @Getter @Setter @NoArgsConstructor
        public static class Properties {
            private String nickname;
            private String profile_image;
            private String thumbnail_image;
        }

        @Getter @Setter @NoArgsConstructor
        public static class KakaoAccount {

            @JsonProperty("profile_nickname_needs_agreement")
            private Boolean profileNicknameNeedsAgreement;

            @JsonProperty("profile_image_needs_agreement")
            private Boolean profileImageNeedsAgreement;

            private Profile profile;

            @JsonProperty("has_email")
            private Boolean hasEmail;

            @JsonProperty("email_needs_agreement")
            private Boolean emailNeedsAgreement;

            @JsonProperty("is_email_valid")
            private Boolean isEmailValid;

            @JsonProperty("is_email_verified")
            private Boolean isEmailVerified;

            private String email;

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter @Setter @NoArgsConstructor
            public static class Profile {
                private String nickname;

                @JsonProperty("thumbnail_image_url")
                private String thumbnailImageUrl;

                @JsonProperty("profile_image_url")
                private String profileImageUrl;

                @JsonProperty("is_default_nickname")
                private Boolean isDefaultNickname;

                @JsonProperty("is_default_image")
                private Boolean isDefaultImage;
            }
        }
    }
}
