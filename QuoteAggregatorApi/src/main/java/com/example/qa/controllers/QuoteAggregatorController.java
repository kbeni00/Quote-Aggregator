package com.example.qa.controllers;

import com.example.qa.entities.Quote;
import com.example.qa.services.QuoteAggregatorService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/quotes")
public class QuoteAggregatorController {
    private final QuoteAggregatorService quoteAggregatorService;

    public QuoteAggregatorController(QuoteAggregatorService quoteAggregatorService) {
        this.quoteAggregatorService = quoteAggregatorService;
    }

    @GetMapping("/simpsons")
    public Mono<Optional<Quote>> getSimpsonQuotes() {
        return quoteAggregatorService.getSimpsonsQuote();
    }

    @GetMapping("/simpsons/filtered")
    public Mono<Optional<Quote>> getSimpsonsFilteredQuotes(@RequestParam String character) {
        return quoteAggregatorService.getFilteredSimpsonsQuote(character);
    }

    @GetMapping("/ninjas")
    public Mono<Optional<Quote>> getNinjasQuote() {
        return quoteAggregatorService.getNinjasQuote();
    }

    @PostMapping("/{id}/vote")
    public Quote voteForQuote(@PathVariable Long id, @RequestBody Map<String, String> requestData) {
        String userEmail = requestData.get("userEmail");
        if (userEmail == null || userEmail.isEmpty()) {
            throw new RuntimeException("User email is required to vote.");
        }
        return quoteAggregatorService.voteForQuote(id, userEmail);
    }

    @GetMapping("/top")
    public List<Quote> getTopQuotes(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        return quoteAggregatorService.getTopQuotes(limit);
    }

    @GetMapping("/quote-sources")
    public Mono<Map<String, String>> getQuoteSources() {
        Map<String, String> quoteSources = new HashMap<>();
        quoteSources.put("simpsons", "https://thesimpsonsquoteapi.glitch.me");
        quoteSources.put("ninjas", "https://api.api-ninjas.com/v1");

        return Mono.just(quoteSources);
    }
}
