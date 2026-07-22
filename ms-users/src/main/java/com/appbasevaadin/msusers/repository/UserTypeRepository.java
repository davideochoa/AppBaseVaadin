package com.appbasevaadin.msusers.repository;

import com.appbasevaadin.msusers.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTypeRepository extends JpaRepository<UserType, Long> {
}
