package com.example.qa.repositories;

import com.example.qa.entities.Quote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Optional<Quote> findByQuoteText(String quoteText);
    List<Quote> findByOrderByVotesDesc(Pageable pageable);
}
