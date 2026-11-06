package com.dappunq.service;

import com.dappunq.dto.AuthResult;
import com.dappunq.dto.UserRequestDTO;
import com.dappunq.dto.UserResponseDTO;

public interface AuthService {

    AuthResult login(UserRequestDTO request);

    UserResponseDTO register(UserRequestDTO request);
}
