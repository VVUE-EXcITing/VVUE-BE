package com.exciting.vvue.user.service;


import com.exciting.vvue.auth.oauth.model.OAuthUserInfo;
import com.exciting.vvue.user.exception.UserNotFoundException;
import com.exciting.vvue.user.model.User;
import com.exciting.vvue.user.model.dto.UserDto;
import com.exciting.vvue.user.model.dto.UserModifyDto;

public interface UserService {

    UserDto getUserDto(Long userId) throws UserNotFoundException;

    User getUserByProviderId(String provider, String providerId);

    User addOAuthUser(OAuthUserInfo oauthUser);

    void modifyUser(Long userId, UserModifyDto userModifyDto) throws UserNotFoundException;

    void delete(Long userId) throws UserNotFoundException;

    User getUserById(Long userId);
}

