package com.dappunq.service;

import com.dappunq.dto.UserRequestDTO;
import com.dappunq.dto.UserResponseDTO;

public interface AuthService {

    AuthServiceImpl.AuthResult login(UserRequestDTO request);

    UserResponseDTO register(UserRequestDTO request);
}
