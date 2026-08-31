package com.martin.demo.repository;

import com.martin.demo.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByIdAndActiveTrue(Long id);

    @Query("""
        SELECT a FROM Application a
        WHERE a.active = true
        AND (a.sender.username = :username OR a.respondedBy.username = :username)
        ORDER BY a.updatedAt DESC
    """)
    List<Application> findActiveForUser(String username);

    @Query("""
        SELECT a FROM Application a
        WHERE a.active = true
        AND a.status IN (com.martin.demo.model.ApplicationStatus.PENDING, com.martin.demo.model.ApplicationStatus.COUNTERED)
        AND a.sender.username = :username
        ORDER BY a.updatedAt DESC
    """)
    List<Application> findActiveSentByUser(String username);

    @Query("""
        SELECT a FROM Application a
        WHERE a.active = true
        AND a.status = com.martin.demo.model.ApplicationStatus.PENDING
        AND a.respondedBy IS NULL
        ORDER BY a.updatedAt DESC
    """)
    List<Application> findPendingForReceivers();

    @Query("""
        SELECT a FROM Application a
        WHERE a.status IN (com.martin.demo.model.ApplicationStatus.ACCEPTED, com.martin.demo.model.ApplicationStatus.DECLINED)
        AND (a.sender.username = :username OR a.respondedBy.username = :username)
        ORDER BY a.updatedAt DESC
    """)
    List<Application> findClosedForUser(String username);
}
