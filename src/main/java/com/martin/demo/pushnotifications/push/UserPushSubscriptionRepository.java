package com.martin.demo.pushnotifications.push;

import com.martin.demo.pushnotifications.push.UserPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPushSubscriptionRepository extends JpaRepository<UserPushSubscription, Long> {
    List<UserPushSubscription> findByUserUsername(String username);

    boolean existsByEndpoint(String endpoint);

    Optional<UserPushSubscription> findByEndpoint(String endpoint);

    void deleteByEndpoint(String endpoint);

    List<UserPushSubscription> findAllByUserId(Long userId);
}
