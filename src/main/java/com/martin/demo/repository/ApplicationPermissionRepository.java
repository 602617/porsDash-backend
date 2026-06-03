package com.martin.demo.repository;

import com.martin.demo.model.ApplicationPermission;
import com.martin.demo.model.ApplicationPermissionRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationPermissionRepository extends JpaRepository<ApplicationPermission, Long> {

    List<ApplicationPermission> findByRole(ApplicationPermissionRole role);

    boolean existsByUserIdAndRole(Long userId, ApplicationPermissionRole role);
}
