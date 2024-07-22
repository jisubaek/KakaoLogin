package com.example.kakaologin.service;

import com.example.kakaologin.model.User;
import com.example.kakaologin.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerOrUpdateUser(String username, String email, String profileImageUrl) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setUsername(username);
            user.setProfileImageUrl(profileImageUrl);
        } else {
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setProfileImageUrl(profileImageUrl);
        }
        return userRepository.save(user);
    }
}
