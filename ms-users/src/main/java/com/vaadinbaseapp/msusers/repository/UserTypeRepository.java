package com.vaadinbaseapp.msusers.repository;

import com.vaadinbaseapp.msusers.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTypeRepository extends JpaRepository<UserType, Long> {
}
