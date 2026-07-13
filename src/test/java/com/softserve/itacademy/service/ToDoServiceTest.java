package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.ToDoRepository;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToDoServiceTest {

    @Mock
    private ToDoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ToDoService todoService;

    private ToDo todo;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(2L);
        user.setFirstName("Collaborator");

        todo = new ToDo();
        todo.setId(1L);
        todo.setTitle("Shopping");
        todo.setCollaborators(new HashSet<>());
    }

    @Test
    void create_ShouldReturnSavedToDo_WhenTitleIsUnique() {
        when(todoRepository.existsByTitle(todo.getTitle())).thenReturn(false);
        when(todoRepository.save(any(ToDo.class))).thenReturn(todo);

        ToDo savedToDo = todoService.create(todo);

        assertNotNull(savedToDo);
        assertEquals(todo.getTitle(), savedToDo.getTitle());
        verify(todoRepository).save(todo);
    }

    @Test
    void create_ShouldThrowException_WhenTitleExists() {
        when(todoRepository.existsByTitle(todo.getTitle())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> todoService.create(todo));
        verify(todoRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenToDoIsNull() {
        assertThrows(NullEntityReferenceException.class, () -> todoService.create(null));
    }

    @Test
    void readById_ShouldReturnToDo_WhenExists() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        ToDo foundToDo = todoService.readById(1L);

        assertEquals(todo, foundToDo);
    }

    @Test
    void readById_ShouldThrowException_WhenNotFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> todoService.readById(1L));
    }

    @Test
    void update_ShouldReturnUpdatedToDo_WhenValid() {
        when(todoRepository.existsByTitleAndIdNot(todo.getTitle(), 1L)).thenReturn(false);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        ToDo updatedToDo = todoService.update(todo);

        assertEquals(todo, updatedToDo);
    }

    @Test
    void update_ShouldThrowException_WhenToDoIsNull() {
        assertThrows(NullEntityReferenceException.class, () -> todoService.update(null));
    }

    @Test
    void delete_ShouldCallRepository_WhenExists() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        todoService.delete(1L);

        verify(todoRepository).delete(todo);
    }

    @Test
    void getAll_ShouldReturnList() {
        List<ToDo> todos = Arrays.asList(todo);
        when(todoRepository.findAll()).thenReturn(todos);

        List<ToDo> result = todoService.getAll();

        assertEquals(1, result.size());
        assertEquals("Shopping", result.get(0).getTitle());
    }

    @Test
    void getByUserId_ShouldReturnList() {
        List<ToDo> todos = Arrays.asList(todo);
        when(todoRepository.getByUserId(2L)).thenReturn(todos);

        List<ToDo> result = todoService.getByUserId(2L);

        assertEquals(1, result.size());
    }

    @Test
    void addCollaborator_ShouldAddUser_WhenUserAndToDoExist() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(todoRepository.save(todo)).thenReturn(todo);

        todoService.addCollaborator(1L, 2L);

        assertTrue(todo.getCollaborators().contains(user));
        verify(todoRepository).save(todo);
    }

    @Test
    void addCollaborator_ShouldThrowException_WhenUserNotFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> todoService.addCollaborator(1L, 2L));
        verify(todoRepository, never()).save(any());
    }

    @Test
    void removeCollaborator_ShouldRemoveUser_WhenUserAndToDoExist() {
        todo.getCollaborators().add(user);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(todoRepository.save(todo)).thenReturn(todo);

        todoService.removeCollaborator(1L, 2L);

        assertFalse(todo.getCollaborators().contains(user));
        verify(todoRepository).save(todo);
    }

    @Test
    void removeCollaborator_ShouldThrowException_WhenUserNotFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> todoService.removeCollaborator(1L, 2L));
        verify(todoRepository, never()).save(any());
    }
}