package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.sql.init.mode=never")
public class ToDoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ToDoRepository todoRepository;

    @Test
    void getByUserId_ShouldReturnToDos() {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("owner@mail.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
        entityManager.persist(user);

        ToDo todo1 = new ToDo();
        todo1.setTitle("ToDo1");
        todo1.setOwner(user);
        todo1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(todo1);

        ToDo todo2 = new ToDo();
        todo2.setTitle("ToDo2");
        todo2.setOwner(user);
        todo2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(todo2);

        entityManager.flush();

        List<ToDo> todos = todoRepository.getByUserId(user.getId());

        assertEquals(2, todos.size());
        assertTrue(todos.stream().anyMatch(t -> t.getTitle().equals("ToDo1")));
        assertTrue(todos.stream().anyMatch(t -> t.getTitle().equals("ToDo2")));
    }

    @Test
    void existsByTitle_ShouldReturnTrue_WhenExists() {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("exists@mail.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
        entityManager.persist(user);

        ToDo todo = new ToDo();
        todo.setTitle("ExistingTitle");
        todo.setOwner(user);
        todo.setCreatedAt(LocalDateTime.now());
        entityManager.persist(todo);
        entityManager.flush();

        assertTrue(todoRepository.existsByTitle("ExistingTitle"));
        assertFalse(todoRepository.existsByTitle("NonExistingTitle"));
    }

    @Test
    void existsByTitleAndIdNot_ShouldWorkCorrectly() {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("test3@mail.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
        entityManager.persist(user);

        ToDo todo1 = new ToDo();
        todo1.setTitle("Title1");
        todo1.setOwner(user);
        todo1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(todo1);

        ToDo todo2 = new ToDo();
        todo2.setTitle("Title2");
        todo2.setOwner(user);
        todo2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(todo2);
        entityManager.flush();

        assertFalse(todoRepository.existsByTitleAndIdNot("Title1", todo1.getId()));
        assertTrue(todoRepository.existsByTitleAndIdNot("Title2", todo1.getId()));
    }
}