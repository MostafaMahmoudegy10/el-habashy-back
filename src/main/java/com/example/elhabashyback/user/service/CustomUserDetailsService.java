package com.example.elhabashyback.user.service;

import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository  userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        Users user = userRepository.findUsersByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email you entered is invalid "+ email));

        return new CustomUserDetails(user);
    }
}
