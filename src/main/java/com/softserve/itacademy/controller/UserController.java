package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserDtoConverter userDtoConverter;

    @GetMapping("/create")
    public String create(Model model) {
        log.debug("GET request for user creation");
        model.addAttribute("user", new CreateUserDto());
        return "create-user";
    }

    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") CreateUserDto userDto,
                        BindingResult result) {
        log.info("POST request to create user with email: {}", userDto.getEmail());
        if (result.hasErrors()) {
            log.warn("Validation failed for user creation: {}", result.getAllErrors());
            return "create-user";
        }
        try {
            User user = userService.register(userDto);
            log.debug("User '{}' created successfully", user.getFirstName());
            return "redirect:/todos/all/users/" + user.getId();
        } catch (IllegalArgumentException e) {
            log.warn("Error creating user: {}", e.getMessage());
            result.rejectValue("email", "error.user", e.getMessage());
            return "create-user";
        }
    }

    @GetMapping("/{id}/read")
    public String read(@PathVariable("id") Long id, Model model) {
        log.info("GET request to read user with id: {}", id);
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/{id}/update")
    public String update(@PathVariable("id") Long id, Model model) {
        log.debug("GET request for user update form, ID: {}", id);

        User user = userService.readById(id);
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        
        model.addAttribute("user", userDto);
        model.addAttribute("roles", UserRole.values());
        return "update-user";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Long id,
                        @Validated @ModelAttribute("user") UpdateUserDto userDto,
                        BindingResult result,
                        Model model) {
        log.info("POST request to update user ID: {}", id);
        if (result.hasErrors()) {
            log.warn("Validation failed for user update ID {}: {}", id, result.getAllErrors());
            model.addAttribute("roles", UserRole.values());
            return "update-user";
        }
        userDto.setId(id);
        userService.update(userDto);

        log.debug("User ID {} updated successfully", id);

        return "redirect:/users/all";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        log.info("GET request to delete user ID: {}", id);
        userService.delete(id);
        log.debug("User ID {} deleted successfully", id);
        return "redirect:/users/all";
    }

    @GetMapping("/all")
    public String getAll(Model model) {
        log.debug("GET request to retrieve all users");
        model.addAttribute("users", userService.getAll());
        return "users-list";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }
}
