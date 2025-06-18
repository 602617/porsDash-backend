package com.martin.demo.repository;

import com.martin.demo.model.Items;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Items,Long> {
}
