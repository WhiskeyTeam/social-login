package com.example.socialauth.oauth2;

import com.example.socialauth.entity.member.LoginType;

public enum Provider {
    BASIC, Naver, Google;

    public LoginType toLoginType() {
        switch (this) {
            case Naver:
                return LoginType.NAVER;
            case Google:
                return LoginType.GOOGLE;
            default:
                return LoginType.BASIC;
        }
    }
}
