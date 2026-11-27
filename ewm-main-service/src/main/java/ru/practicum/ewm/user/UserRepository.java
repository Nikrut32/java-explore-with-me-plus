package ru.practicum.ewm.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}