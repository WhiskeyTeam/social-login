package com.example.socialauth.oauth2;

import com.example.socialauth.entity.Member;

public enum Provider {
    BASIC, Naver, Google;

    public Member.LoginType toLoginType() {
    }
}
