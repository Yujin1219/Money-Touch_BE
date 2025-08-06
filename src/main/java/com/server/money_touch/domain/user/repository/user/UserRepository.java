package com.server.money_touch.domain.user.repository.user;

import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 전체 유저 조회
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userDetail")
    List<User> findAllWithUserDetail();

    // email로 유저 조회
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
    
}
