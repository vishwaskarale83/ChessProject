package com.vishwask.chess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(username, email, passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}