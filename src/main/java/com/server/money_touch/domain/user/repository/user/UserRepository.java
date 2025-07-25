package com.server.money_touch.domain.user.repository.user;

import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // 전체 유저 조회
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userDetail")
    List<User> findAllWithUserDetail();
}
