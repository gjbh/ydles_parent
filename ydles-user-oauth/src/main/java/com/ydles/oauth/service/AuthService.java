package com.ydles.oauth.service;

import com.ydles.oauth.util.AuthToken;

public interface AuthService {
    AuthToken login(String username, String password, String clientId, String clientSecret);
}
