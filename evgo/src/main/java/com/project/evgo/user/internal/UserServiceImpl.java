package com.project.evgo.user.internal;

import com.project.evgo.user.UserService;
import com.project.evgo.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDtoConverter userDtoConverter;

    @Override
    public Optional<UserResponse> findById(Long id) {
        return userDtoConverter.convert(userRepository.findById(id));
    }

    @Override
    public List<UserResponse> findAll() {
        return userDtoConverter.convert(userRepository.findAll());
    }
}
