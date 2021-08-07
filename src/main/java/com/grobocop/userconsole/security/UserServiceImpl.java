package com.grobocop.userconsole.security;

import com.grobocop.userconsole.data.UserEntity;
import com.grobocop.userconsole.data.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserEntityByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username " + username + " does not exist."));
    }


    @Override
    public UserEntity addUser(String username, String rawPassword) {
        final UserEntity user = new UserEntity(null,
                username,
                passwordEncoder.encode(rawPassword),
                "USER",
                false,
                true);
        return userRepository.save(user);
    }

    @Override
    public UserEntity addAdmin(String username, String rawPassword) {
        final UserEntity user = new UserEntity(null,
                username,
                passwordEncoder.encode(rawPassword),
                "USER,ADMIN",
                false,
                true);
        return userRepository.save(user);
    }
}
