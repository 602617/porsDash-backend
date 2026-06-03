package com.martin.demo.repository;

import com.martin.demo.model.ApplicationOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationOfferRepository extends JpaRepository<ApplicationOffer, Long> {
    List<ApplicationOffer> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);
}
