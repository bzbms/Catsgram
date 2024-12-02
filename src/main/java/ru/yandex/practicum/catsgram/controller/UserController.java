package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
        private final Map<Long, User> users = new HashMap<>();

        @GetMapping
        public Collection<User> findAll() {
            return users.values();
        }

        @PostMapping
        public User create(@RequestBody User user) {
            if (user.getUsername() == null || user.getUsername().isBlank()) {
                throw new ConditionsNotMetException("Имя не может быть пустым");
            }
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw new ConditionsNotMetException("Пароль не может быть пустым");
            }
            String email = user.getEmail();
            if (email == null || email.isBlank()) {
                throw new ConditionsNotMetException("Имейл должен быть указан");
            }
            if (users.values().stream().anyMatch(user1 -> user1.getEmail().equals(email))) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            user.setId(getNextId());
            user.setRegistrationDate(Instant.now());
            users.put(user.getId(), user);
            return user;
        }

        private long getNextId() {
            long currentMaxId = users.keySet()
                    .stream()
                    .mapToLong(id -> id)
                    .max()
                    .orElse(0);
            return ++currentMaxId;
        }

        @PutMapping
        public User update(@RequestBody User newUser) {
            if (newUser.getId() == null) {
                throw new ConditionsNotMetException("Id должен быть указан");
            }
            if (users.containsKey(newUser.getId())) {
                User oldUser = users.get(newUser.getId());
                if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
                    throw new ConditionsNotMetException("Пароль не может быть пустым");
                }
                String email = newUser.getEmail();
                if (!(email == null || email.isBlank())) {
                    if (users.values().stream().anyMatch(user1 -> user1.getEmail().equals(email))) {
                        throw new DuplicatedDataException("Этот имейл уже используется");
                    }
                    oldUser.setEmail(email);
                }
                oldUser.setUsername(newUser.getUsername());
                return oldUser;
            }
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

}
