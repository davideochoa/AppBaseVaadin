package com.appbasevaadin.mssecurity.repository;

import com.appbasevaadin.mssecurity.entity.SecurityUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityUserRepository extends JpaRepository<SecurityUser, Long> {

    Optional<SecurityUser> findByEmailIgnoreCase(String email);

    Optional<SecurityUser> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
