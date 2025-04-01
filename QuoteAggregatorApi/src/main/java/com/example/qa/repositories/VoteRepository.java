package com.example.qa.repositories;

import com.example.qa.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByQuoteIdAndUserId(Long quoteId, String userId);
}
