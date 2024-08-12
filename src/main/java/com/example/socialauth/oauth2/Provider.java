package com.example.socialauth.oauth2;

import com.example.socialauth.entity.Member;

public enum Provider {
    BASIC, Naver, Google;

    public Member.LoginType toLoginType() {
        switch (this) {
            case Naver:
                return Member.LoginType.NAVER;
            case Google:
                return Member.LoginType.GOOGLE;
            default:
                return Member.LoginType.BASIC;
        }
    }
}
