package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserDtoConverter userDtoConverter;

    @Transactional
    public User register(CreateUserDto createUserDto) {
        log.info("Registering a new user with email: {}", createUserDto.getEmail());
        createUserDto.setRole(UserRole.USER);
        User user = userDtoConverter.convertToUser(createUserDto);
        user.setPassword("{noop}" + user.getPassword());
        return create(user);
    }

    @Transactional
    public User create(User user) {
        log.info("Creating a new user with email: {}", user.getEmail());
        if (user != null) {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                log.warn("Attempted to create a user that already exists: {}", user.getEmail());
                throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
            }
            User savedUser = userRepository.save(user);
            log.debug("User saved with ID: {}", savedUser.getId());
            return savedUser;
        }
        log.error("Attempted to create a null user");
        throw new NullEntityReferenceException("User cannot be 'null'");
    }

    @Transactional(readOnly = true)
    public User readById(long id) {
        log.debug("Reading user by ID: {}", id);
        return userRepository.findById(id).orElseThrow(
                () -> {
                    log.error("User with ID {} not found", id);
                    return new EntityNotFoundException("User with id " + id + " not found");
                });
    }

    @Transactional
    public UserDto update(UpdateUserDto updateUserDto) {
        log.info("Updating user with ID: {}", updateUserDto.getId());
        User user = userRepository.findById(updateUserDto.getId()).orElseThrow(
                () -> {
                    log.warn("Attempted to update a non existing user with id: {}", updateUserDto.getId());
                    return new EntityNotFoundException("User with id " + updateUserDto.getId() + " not found");
                });
        if (updateUserDto.getRole() != null && user.getRole() == UserRole.ADMIN) {
            user.setRole(updateUserDto.getRole());
            updateUserDto.setRole(null);
        } else {
            updateUserDto.setRole(null);
        }
        userDtoConverter.fillFields(user, updateUserDto);
        User savedUser = userRepository.save(user);
        log.debug("User with ID {} successfully updated", savedUser.getId());

        return userDtoConverter.toDto(user);
    }

    @Transactional
    public void delete(long id) {
        log.info("Deleting user with ID: {}", id);
        User user = readById(id);
        userRepository.delete(user);
        log.debug("User with ID {} successfully deleted", id);
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Searching for user by name: {}", username);
        return userRepository.findByEmail(username);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findById(long id) {
        log.debug("Retrieving optional with userDto by user ID: {}", id);
        return userRepository.findById(id).map(userDtoConverter::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto findByIdThrowing(long id) {
        log.debug("Retrieving userDto by user ID: {}", id);
        return userRepository.findById(id).map(userDtoConverter::toDto).orElseThrow(() -> {
            log.error("User with ID {} not found, cannot create appropriate dto", id);
            return new EntityNotFoundException();
        });
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        log.debug("Fetching all users as DTOs");
        return userRepository.findAll().stream().map(userDtoConverter::toDto).toList();
    }
}
