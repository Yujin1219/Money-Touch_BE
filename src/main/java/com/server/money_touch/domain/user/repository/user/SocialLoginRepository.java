package com.server.money_touch.domain.user.repository.user;

import com.server.money_touch.domain.user.entity.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

}
