package com.martin.demo.repository;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.Friendship;
import com.martin.demo.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByRequesterAndAddressee(AppUser requester, AppUser addressee);

    @Query("""
        SELECT f FROM Friendship f
        WHERE f.status = :status
          AND (f.requester.id = :userId OR f.addressee.id = :userId)
    """)
    List<Friendship> findAllByUserAndStatus(@Param("userId") Long userId,
                                            @Param("status") FriendshipStatus status);

    List<Friendship> findByAddresseeAndStatus(AppUser addressee, FriendshipStatus status);
}
