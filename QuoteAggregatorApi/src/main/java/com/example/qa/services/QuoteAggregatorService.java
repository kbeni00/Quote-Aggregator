package com.example.qa.services;

import com.example.qa.entities.Quote;
import com.example.qa.entities.Vote;
import com.example.qa.models.NinjasModel;
import com.example.qa.models.SimpsonsModel;
import com.example.qa.repositories.QuoteRepository;
import com.example.qa.repositories.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class QuoteAggregatorService {
    private final WebClient simpsonsClient;
    private final WebClient ninjasClient;
    private final String ninjasAPIKey;
    private final QuoteRepository quoteRepository;
    private final VoteRepository voteRepository;


    @Autowired
    public QuoteAggregatorService(@Value("${web.api.apiKey}") String ninjasAPIKey, WebClient.Builder webClientBuilder, QuoteRepository quoteRepository, VoteRepository voteRepository) {
        this.simpsonsClient = webClientBuilder.baseUrl("https://thesimpsonsquoteapi.glitch.me").build();
        this.ninjasClient = webClientBuilder.baseUrl("https://api.api-ninjas.com/v1").build();
        this.ninjasAPIKey = ninjasAPIKey;
        this.quoteRepository = quoteRepository;
        this.voteRepository = voteRepository;
    }

    public Mono<Optional<Quote>> getSimpsonsQuote() {
        return simpsonsClient
                .get()
                .uri("/quotes")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SimpsonsModel[].class)
                .map(quotes ->
                {
                    SimpsonsModel simpsonsModel = quotes[0];
                    saveQuote(Quote.builder()
                            .quoteText(simpsonsModel.getQuote())
                            .image(simpsonsModel.getImage())
                            .characterDirection(simpsonsModel.getCharacterDirection())
                            .character(simpsonsModel.getCharacter())
                            .source("simpsons")
                            .build()
                    );
                    return convertModelToQuote(simpsonsModel);
                });
    }

    public Mono<Optional<Quote>> getFilteredSimpsonsQuote(String character) {
        return simpsonsClient
                .get()
                .uri("/quotes?character=" + character)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SimpsonsModel[].class)
                .map(quotes ->
                {
                    SimpsonsModel simpsonsModel = quotes[0];
                    saveQuote(Quote.builder()
                            .quoteText(simpsonsModel.getQuote())
                            .image(simpsonsModel.getImage())
                            .characterDirection(simpsonsModel.getCharacterDirection())
                            .character(simpsonsModel.getCharacter())
                            .source("simpsons")
                            .build()
                    );
                    return convertModelToQuote(simpsonsModel);
                });

    }

    public Mono<Optional<Quote>> getNinjasQuote() {
        return ninjasClient
                .get()
                .uri("/quotes")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Api-Key", ninjasAPIKey)
                .retrieve()
                .bodyToMono(NinjasModel[].class)
                .map(quotes ->
                {
                    NinjasModel ninjasModel = quotes[0];
                    saveQuote(Quote.builder()
                            .quoteText(ninjasModel.getQuote())
                            .author(ninjasModel.getAuthor())
                            .category(ninjasModel.getCategory())
                            .source("ninjas")
                            .build()
                    );
                    return convertNinjasModelToQuote(ninjasModel);
                });
    }

    public List<Quote> getTopQuotes(int limit) {
        int validLimit = Math.min(Math.max(limit, 5), 20);
        return quoteRepository.findByOrderByVotesDesc(PageRequest.of(0, validLimit));
    }

    public Quote voteForQuote(Long quoteId, String userId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + quoteId));

        // Check if the user has already voted
        boolean hasVoted = voteRepository.existsByQuoteIdAndUserId(quoteId, userId);
        if (hasVoted) {
            throw new RuntimeException("User has already voted for this quote.");
        }

        // Save the vote
        Vote vote = new Vote(null, quote, userId);
        voteRepository.save(vote);

        // Increment vote count
        quote.setVotes(quote.getVotes() + 1);
        return quoteRepository.save(quote);
    }


    private Quote saveQuote(Quote quoteToBeSaved) {
        Optional<Quote> existingQuoteEntity = quoteRepository.findByQuoteText(quoteToBeSaved.getQuoteText());

        if (existingQuoteEntity.isPresent()) {
            return existingQuoteEntity.get();
        } else if(quoteToBeSaved.getSource().equals("simpsons")){
            Quote newQuote = new Quote();
            newQuote.setQuoteText(quoteToBeSaved.getQuoteText());
            newQuote.setCharacter(quoteToBeSaved.getCharacter());
            newQuote.setSource("simpsons");
            newQuote.setImage(quoteToBeSaved.getImage());
            newQuote.setCharacterDirection(quoteToBeSaved.getCharacterDirection());
            newQuote.setVotes(0);
            return quoteRepository.save(newQuote);
        } else{
            Quote newQuote = new Quote();
            String quoteText = quoteToBeSaved.getQuoteText();
            if (quoteText != null && quoteText.length() > 255) {
                quoteText = quoteText.substring(0, 255);
            }
            newQuote.setQuoteText(quoteText);
            newQuote.setAuthor(quoteToBeSaved.getAuthor());
            newQuote.setCategory(quoteToBeSaved.getCategory());
            newQuote.setSource("ninjas");
            newQuote.setVotes(0);
            return quoteRepository.save(newQuote);

        }
    }

    private Optional<Quote> convertNinjasModelToQuote(NinjasModel model) {
        return quoteRepository.findByQuoteText(model.getQuote());

    }

    private Optional<Quote> convertModelToQuote(SimpsonsModel model) {
        return quoteRepository.findByQuoteText(model.getQuote());
    }




}
