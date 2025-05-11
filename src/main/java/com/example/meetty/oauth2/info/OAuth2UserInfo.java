package com.example.meetty.oauth2.info;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getName();
    String getPhoneNumber();
    String getImage();
}
