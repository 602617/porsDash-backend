package com.martin.demo.repository;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingListRepository extends JpaRepository<ShoppingList,Long> {

    List<ShoppingList> findByOwner(AppUser owner);
}
