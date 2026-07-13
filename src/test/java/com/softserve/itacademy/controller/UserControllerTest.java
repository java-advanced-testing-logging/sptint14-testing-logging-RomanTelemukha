package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDtoConverter userDtoConverter;

    @Test
    void createGet_ShouldReturnCreateUserView() throws Exception {
        mockMvc.perform(get("/users/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void createPost_ShouldRedirect_WhenValid() throws Exception {
        User registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setFirstName("John");

        when(userService.register(any(CreateUserDto.class))).thenReturn(registeredUser);

        mockMvc.perform(post("/users/create")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@mail.com")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        verify(userService).register(any(CreateUserDto.class));
    }

    @Test
    void createPost_ShouldReturnForm_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/users/create")
                        .param("firstName", "")
                        .param("email", "invalid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"));

        verify(userService, never()).register(any());
    }

    @Test
    void createPost_ShouldReturnForm_WhenIllegalArgumentExceptionThrown() throws Exception {
        when(userService.register(any(CreateUserDto.class)))
                .thenThrow(new IllegalArgumentException("User with emailAlreadyExists@mail.com already exists"));

        mockMvc.perform(post("/users/create")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "AlreadyExists@mail.com")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().hasErrors());
    }

    @Test
    void read_ShouldReturnUserInfoView() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");

        when(userService.readById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1/read"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-info"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    void updateGet_ShouldReturnUpdateUserView() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setRole(UserRole.USER);

        when(userService.readById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1/update"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    void updatePost_ShouldRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/users/1/update")
                        .param("firstName", "JohnUpdated")
                        .param("lastName", "DoeUpdated")
                        .param("email", "john@mail.com")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/all"));

        verify(userService).update(any(UpdateUserDto.class));
    }

    @Test
    void updatePost_ShouldReturnForm_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/users/1/update")
                        .param("firstName", "")
                        .param("email", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"))
                .andExpect(model().attributeExists("roles"));

        verify(userService, never()).update(any());
    }

    @Test
    void delete_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/users/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/all"));

        verify(userService).delete(1L);
    }

    @Test
    void getAll_ShouldReturnUsersListView() throws Exception {
        when(userService.getAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("users-list"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void shouldHandleEntityNotFoundException() throws Exception {
        when(userService.readById(999L)).thenThrow(new EntityNotFoundException("User with id 999 not found"));

        mockMvc.perform(get("/users/999/read"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("message"));
    }
}