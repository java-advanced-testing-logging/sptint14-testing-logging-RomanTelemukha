package com.softserve.itacademy.service;

import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDtoConverter userDtoConverter;

    @InjectMocks
    private UserService userService;

    private User user;
    private UpdateUserDto updateUserDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);

        updateUserDto = new UpdateUserDto();
        updateUserDto.setId(1L);
        updateUserDto.setFirstName("JohnUpdated");
        updateUserDto.setLastName("DoeUpdated");
        updateUserDto.setEmail("john@mail.com");
        updateUserDto.setRole(UserRole.ADMIN);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("JohnUpdated");
        userDto.setEmail("john@mail.com");
    }

    @Test
    void readById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.readById(1L);

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void readById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.readById(1L));
    }

    @Test
    void update_ShouldUpdateRole_WhenCurrentUserIsAdmin() {
        user.setRole(UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoConverter.toDto(user)).thenReturn(userDto);

        UserDto result = userService.update(updateUserDto);

        assertNotNull(result);
        assertEquals(UserRole.ADMIN, user.getRole());
        assertNull(updateUserDto.getRole());
        verify(userRepository).save(user);
        verify(userDtoConverter).fillFields(user, updateUserDto);
    }

    @Test
    void update_ShouldNotUpdateRole_AndSetItToNullInDto_WhenCurrentUserIsUser() {
        user.setRole(UserRole.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoConverter.toDto(user)).thenReturn(userDto);

        UserDto result = userService.update(updateUserDto);

        assertNotNull(result);
        assertEquals(UserRole.USER, user.getRole());
        assertNull(updateUserDto.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void update_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.update(updateUserDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_ShouldCallRepository_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void getAll_ShouldReturnList() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAll();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }
}