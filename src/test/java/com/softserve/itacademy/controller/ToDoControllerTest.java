package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.todoDto.CreateToDoDto;
import com.softserve.itacademy.dto.todoDto.ToDoDtoConverter;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToDoController.class)
public class ToDoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToDoService todoService;

    @MockBean
    private UserService userService;

    @MockBean
    private ToDoDtoConverter todoDtoConverter;

    @Test
    void createToDoForm_ShouldReturnCreateToDoView() throws Exception {
        mockMvc.perform(get("/todos/create/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().attributeExists("todo"))
                .andExpect(model().attribute("ownerId", 1L));
    }

    @Test
    void createToDo_ShouldRedirect_WhenValid() throws Exception {
        User owner = new User();
        owner.setId(1L);
        ToDo todo = new ToDo();
        todo.setId(5L);

        when(userService.readById(1L)).thenReturn(owner);
        when(todoDtoConverter.toEntity(any(CreateToDoDto.class), any(User.class))).thenReturn(todo);

        mockMvc.perform(post("/todos/create/users/1")
                        .param("title", "New ToDo Submission"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        verify(todoService).create(any(ToDo.class));
    }

    @Test
    void createToDo_ShouldReturnForm_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/todos/create/users/1")
                        .param("title", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().attribute("ownerId", 1L));

        verify(todoService, never()).create(any());
    }

    @Test
    void createToDo_ShouldReturnForm_WhenIllegalArgumentExceptionThrown() throws Exception {
        User owner = new User();
        owner.setId(1L);
        ToDo todo = new ToDo();

        when(userService.readById(1L)).thenReturn(owner);
        when(todoDtoConverter.toEntity(any(CreateToDoDto.class), any(User.class))).thenReturn(todo);
        when(todoService.create(any(ToDo.class))).thenThrow(new IllegalArgumentException("Title already exists"));

        mockMvc.perform(post("/todos/create/users/1")
                        .param("title", "Duplicate Title"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().attribute("ownerId", 1L))
                .andExpect(model().hasErrors());
    }

    @Test
    void updateToDoForm_ShouldReturnUpdateToDoView() throws Exception {
        User owner = new User();
        owner.setId(1L);
        ToDo todo = new ToDo();
        todo.setId(2L);
        todo.setTitle("Old Title");
        todo.setOwner(owner);

        when(todoService.readById(2L)).thenReturn(todo);

        mockMvc.perform(get("/todos/2/update/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-todo"))
                .andExpect(model().attributeExists("todo"));
    }

    @Test
    void updateToDo_ShouldRedirect_WhenValid() throws Exception {
        User owner = new User();
        owner.setId(1L);
        ToDo todo = new ToDo();

        when(todoService.readById(2L)).thenReturn(todo);
        when(userService.readById(1L)).thenReturn(owner);

        mockMvc.perform(post("/todos/2/update/users/1")
                        .param("id", "2")
                        .param("title", "Updated ToDo Title")
                        .param("ownerId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        verify(todoService).update(any(ToDo.class));
    }

    @Test
    void updateToDo_ShouldReturnForm_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/todos/2/update/users/1")
                        .param("title", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("update-todo"));

        verify(todoService, never()).update(any());
    }

    @Test
    void updateToDo_ShouldReturnForm_WhenIllegalArgumentExceptionThrown() throws Exception {
        User owner = new User();
        owner.setId(1L);
        ToDo todo = new ToDo();

        when(todoService.readById(2L)).thenReturn(todo);
        when(userService.readById(1L)).thenReturn(owner);
        when(todoService.update(any(ToDo.class))).thenThrow(new IllegalArgumentException("Title match clash"));

        mockMvc.perform(post("/todos/2/update/users/1")
                        .param("id", "2")
                        .param("title", "Clashing Title")
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-todo"))
                .andExpect(model().hasErrors());
    }

    @Test
    void delete_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/todos/2/delete/users/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        verify(todoService).delete(2L);
    }

    @Test
    void getAll_ShouldReturnTodosUserView() throws Exception {
        User user = new User();
        user.setId(1L);

        when(todoService.getByUserId(1L)).thenReturn(new ArrayList<>());
        when(userService.readById(1L)).thenReturn(user);

        mockMvc.perform(get("/todos/all/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("todos-user"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    void getTasks_ShouldReturnTodoTasksView() throws Exception {
        User owner = new User();
        owner.setId(1L);
        ToDo todo = new ToDo();
        todo.setId(2L);
        todo.setOwner(owner);
        todo.setCollaborators(new java.util.HashSet<>());
        todo.setTasks(new HashSet<>());

        when(todoService.readById(2L)).thenReturn(todo);
        when(userService.getAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/todos/2/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo-tasks"))
                .andExpect(model().attribute("todo", todo))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void addCollaborator_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/todos/2/add").param("user_id", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/2/tasks"));

        verify(todoService).addCollaborator(2L, 3L);
    }

    @Test
    void removeCollaborator_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/todos/2/remove").param("user_id", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/2/tasks"));

        verify(todoService).removeCollaborator(2L, 3L);
    }

    @Test
    void shouldHandleEntityNotFoundException() throws Exception {
        when(todoService.readById(999L)).thenThrow(new EntityNotFoundException("ToDo not found"));

        mockMvc.perform(get("/todos/999/tasks"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("message"));
    }
}