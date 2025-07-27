package com.server.money_touch.domain.user.repository.user;

import com.server.money_touch.domain.user.entity.LocalLogin;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalLoginRepository extends JpaRepository<LocalLogin, Long> {
    Optional <LocalLogin> findByUser(User user);

}
