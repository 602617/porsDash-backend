package com.martin.demo.repository;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.Items;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Items,Long> {
    List<Items> findByUser(AppUser user);
}
