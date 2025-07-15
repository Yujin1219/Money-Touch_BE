package com.server.money_touch.domain.user.respotiroy.user;

import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
