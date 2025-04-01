package com.example.qa;
import com.example.qa.config.GlobalExceptionHandler;
import com.example.qa.config.SecurityConfig;
import com.example.qa.controllers.QuoteAggregatorController;
import com.example.qa.entities.Quote;
import com.example.qa.services.QuoteAggregatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest({QuoteAggregatorController.class, SecurityConfig.class, GlobalExceptionHandler.class})
public class QuoteAggregatorControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private QuoteAggregatorService quoteAggregatorService;

    @Test
    public void testGetSimpsonsQuote() {
        Quote quote = new Quote();
        quote.setQuoteText("D'oh!");
        quote.setCharacter("Homer Simpson");
        quote.setImage("http://example.com/image.png");
        quote.setCharacterDirection("Left");
        quote.setSource("simpsons");

        when(quoteAggregatorService.getSimpsonsQuote())
                .thenReturn(Mono.just(Optional.of(quote)));

        webTestClient.get()
                .uri("/quotes/simpsons")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.quoteText").isEqualTo("D'oh!")
                .jsonPath("$.character").isEqualTo("Homer Simpson");
    }

    @Test
    public void testGetFilteredSimpsonsQuote() {
        Quote quote = new Quote();
        quote.setQuoteText("D'oh!");
        quote.setCharacter("Homer Simpson");
        quote.setImage("http://example.com/image.png");
        quote.setCharacterDirection("Left");
        quote.setSource("simpsons");

        when(quoteAggregatorService.getFilteredSimpsonsQuote(anyString()))
                .thenReturn(Mono.just(Optional.of(quote)));

        webTestClient.get()
                .uri("/quotes/simpsons/filtered?character=Homer Simpson")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.quoteText").isEqualTo("D'oh!")
                .jsonPath("$.character").isEqualTo("Homer Simpson");
    }

    @Test
    public void testGetNinjasQuote() {
        Quote quote = new Quote();
        quote.setQuoteText("Ninja stars everywhere!");
        quote.setSource("ninjas");
        // Add other fields if needed

        when(quoteAggregatorService.getNinjasQuote())
                .thenReturn(Mono.just(Optional.of(quote)));

        webTestClient.get()
                .uri("/quotes/ninjas")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.quoteText").isEqualTo("Ninja stars everywhere!");
    }

    @Test
    public void testVoteForQuote_Success() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setQuoteText("Test quote");
        quote.setVotes(5);

        when(quoteAggregatorService.voteForQuote(anyLong(), anyString()))
                .thenReturn(quote);

        webTestClient.post()
                .uri("/quotes/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userEmail\": \"test@example.com\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.votes").isEqualTo(5);
    }

    @Test
    public void testVoteForQuote_MissingEmail() {
        webTestClient.post()
                .uri("/quotes/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("User email is required to vote.");
    }

    @Test
    public void testVoteForQuote_AlreadyVoted() {
        when(quoteAggregatorService.voteForQuote(anyLong(), anyString()))
                .thenThrow(new RuntimeException("User has already voted for this quote."));

        webTestClient.post()
                .uri("/quotes/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userEmail\": \"test@example.com\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("User has already voted for this quote.");
    }

    @Test
    public void testGetTopQuotes() {
        Quote quote1 = new Quote();
        quote1.setQuoteText("Top quote 1");
        Quote quote2 = new Quote();
        quote2.setQuoteText("Top quote 2");
        List<Quote> quotes = Arrays.asList(quote1, quote2);

        when(quoteAggregatorService.getTopQuotes(10))
                .thenReturn(quotes);

        webTestClient.get()
                .uri("/quotes/top?limit=10")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Quote.class)
                .hasSize(2)
                .contains(quote1, quote2);
    }

    @Test
    public void testGetTopQuotes_DefaultLimit() {
        when(quoteAggregatorService.getTopQuotes(10))
                .thenReturn(List.of());

        webTestClient.get()
                .uri("/quotes/top")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Quote.class);
    }

    @Test
    public void testGetQuoteSources() {
        webTestClient.get()
                .uri("/quotes/quote-sources")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(map -> {
                    assert map.get("simpsons").equals("https://thesimpsonsquoteapi.glitch.me");
                    assert map.get("ninjas").equals("https://api.api-ninjas.com/v1");
                });
    }
}