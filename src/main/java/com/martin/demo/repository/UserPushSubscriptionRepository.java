package com.martin.demo.repository;

import com.martin.demo.model.UserPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPushSubscriptionRepository extends JpaRepository<UserPushSubscription,Long> {
    List<UserPushSubscription> findByUserUsername(String username);
    void deleteByEndpoint(String endpoint);
    boolean existsByEndpoint(String endpoint);
}
