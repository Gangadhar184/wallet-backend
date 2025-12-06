package com.example.wallet.services;

import com.example.wallet.dto.UpdatePasswordRequest;
import com.example.wallet.dto.UpdateUserRequest;
import com.example.wallet.dto.UserDto;
import com.example.wallet.exceptions.ResourceAlreadyExistsException;
import com.example.wallet.exceptions.ResourceNotFoundException;
import com.example.wallet.mappers.UserMapper;
import com.example.wallet.models.User;
import com.example.wallet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toDto(user);
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(()->new ResourceNotFoundException("User not found")    );

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be same as old password");
        }

        if (request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public List<UserDto> searchUsers(String filter,  int page,  int size) {

        int MAX_PAGE_SIZE = 50;
        int MAX_OFFSET = 10000; // safe upper bound

        // validate size
        if (size < 1) size = 10;
        if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;

        // validate page
        if (page < 0) page = 0;
        if (page * size > MAX_OFFSET) {
            page = MAX_OFFSET / size;
        }
        Pageable pageable = PageRequest.of(page, size);

        if (filter == null || filter.trim().isEmpty()) {
            return userRepository
                    .findAll(pageable)
                    .map(userMapper::toDto)
                    .getContent();
        }

        return userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining(
                        filter, filter, filter,
                        pageable
                )
                .map(userMapper::toDto)
                .getContent();
    }


    @Transactional
    public UserDto updateCurrentUser(UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Email already in use");
            }
        }

        userMapper.updateEntityFromDto(request, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Email already in use");
            }
        }

        userMapper.updateEntityFromDto(request, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

}

