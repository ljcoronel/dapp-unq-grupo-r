package com.dappunq.controller;

import com.dappunq.dto.UserResponseDTO;
import com.dappunq.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserControllerRest {
    private final UserService userService;

    public UserControllerRest(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}/")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
