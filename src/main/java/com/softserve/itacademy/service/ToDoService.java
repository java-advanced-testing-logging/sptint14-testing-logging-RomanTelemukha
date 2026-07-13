package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.ToDoRepository;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToDoService {
    private final ToDoRepository todoRepository;
    private final UserRepository userRepository;

    @Transactional
    public ToDo create(ToDo todo) {
        log.info("Creating a new ToDo with title: {}", todo != null ? todo.getTitle() : null);
        if (todo != null) {
            if (todoRepository.existsByTitle(todo.getTitle())) {
                log.warn("Attempted to create a ToDo that already exists: {}", todo.getTitle());
                throw new IllegalArgumentException("ToDo with title '" + todo.getTitle() + "' already exists");
            }
            ToDo savedTodo = todoRepository.save(todo);
            log.debug("ToDo saved with ID: {}", savedTodo.getId());
            return savedTodo;
        }
        log.error("Attempted to create a null ToDo");
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Transactional(readOnly = true)
    public ToDo readById(long id) {
        log.debug("Reading ToDo by ID: {}", id);
        return todoRepository.findById(id).orElseThrow(
                () -> {
                    log.error("ToDo with ID {} not found", id);
                    return new EntityNotFoundException("ToDo with id " + id + " not found");
                });
    }

    @Transactional
    public ToDo update(ToDo todo) {
        log.info("Updating ToDo with ID: {}", todo != null ? todo.getId() : null);
        if (todo != null) {
            if (todoRepository.existsByTitleAndIdNot(todo.getTitle(), todo.getId())) {
                log.warn("Attempted to update ToDo {} to title '{}' which already exists", todo.getId(), todo.getTitle());
                throw new IllegalArgumentException("ToDo with title '" + todo.getTitle() + "' already exists");
            }
            readById(todo.getId());
            ToDo updatedTodo = todoRepository.save(todo);
            log.debug("ToDo with ID {} successfully updated", updatedTodo.getId());
            return updatedTodo;
        }
        log.error("Attempted to update a null ToDo");
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Transactional
    public void delete(long id) {
        log.info("Deleting ToDo with ID: {}", id);
        ToDo todo = readById(id);
        todoRepository.delete(todo);
        log.debug("ToDo with ID {} successfully deleted", id);
    }

    @Transactional(readOnly = true)
    public List<ToDo> getAll() {
        log.debug("Fetching all ToDos");
        return todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ToDo> getByUserId(long userId) {
        log.debug("Fetching ToDos for user ID: {}", userId);
        return todoRepository.getByUserId(userId);
    }

    @Transactional
    public void addCollaborator(long todoId, long userId) {
        log.info("Adding collaborator (User ID: {}) to ToDo (ID: {})", userId, todoId);
        ToDo todo = readById(todoId);
        User collaborator = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found, cannot add as collaborator", userId);
                    return new EntityNotFoundException("User with id " + userId + " not found");
                });
        todo.getCollaborators().add(collaborator);
        update(todo);
        log.debug("Collaborator {} successfully added to ToDo {}", userId, todoId);
    }

    @Transactional
    public void removeCollaborator(long todoId, long userId) {
        log.info("Removing collaborator (User ID: {}) from ToDo (ID: {})", userId, todoId);
        ToDo todo = readById(todoId);
        User collaborator = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found, cannot remove collaborator", userId);
                    return new EntityNotFoundException("User with id " + userId + " not found");
                });
        todo.getCollaborators().remove(collaborator);
        update(todo);
        log.debug("Collaborator {} successfully removed from ToDo {}", userId, todoId);
    }
}