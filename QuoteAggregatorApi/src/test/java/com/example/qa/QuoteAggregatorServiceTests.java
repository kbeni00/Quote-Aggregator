package com.example.qa;

import com.example.qa.models.NinjasModel;
import com.example.qa.models.SimpsonsModel;
import com.example.qa.services.QuoteAggregatorService;
import com.example.qa.entities.Quote;
import com.example.qa.repositories.QuoteRepository;
import com.example.qa.repositories.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuoteAggregatorServiceTests {

    @Test
    public void testGetSimpsonsQuote() {
        WebClient.Builder builder = Mockito.mock(WebClient.Builder.class);
        WebClient simpsonsClient = Mockito.mock(WebClient.class);
        WebClient ninjasClient = Mockito.mock(WebClient.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(simpsonsClient, ninjasClient);

        // Mock Simpsons client chain
        WebClient.RequestHeadersUriSpec simpsonsUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec simpsonsHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec simpsonsResponseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(simpsonsClient.get()).thenReturn(simpsonsUriSpec);
        when(simpsonsUriSpec.uri("/quotes")).thenReturn(simpsonsHeadersSpec);
        when(simpsonsHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(simpsonsHeadersSpec);
        when(simpsonsHeadersSpec.retrieve()).thenReturn(simpsonsResponseSpec);

        SimpsonsModel[] quotes = new SimpsonsModel[1];
        SimpsonsModel simpsonsQuote = new SimpsonsModel();
        simpsonsQuote.setQuote("Test quote");
        simpsonsQuote.setCharacter("Test character");
        simpsonsQuote.setImage("http://example.com/image.png");
        simpsonsQuote.setCharacterDirection("Left");
        quotes[0] = simpsonsQuote;

        when(simpsonsResponseSpec.bodyToMono(SimpsonsModel[].class))
                .thenReturn(Mono.just(quotes));

        // Mock repositories
        QuoteRepository quoteRepository = Mockito.mock(QuoteRepository.class);
        VoteRepository voteRepository = Mockito.mock(VoteRepository.class);

        // Mock repository interactions
        Quote savedQuote = new Quote();
        savedQuote.setQuoteText("Test quote");
        savedQuote.setCharacter("Test character");
        savedQuote.setImage("http://example.com/image.png");
        savedQuote.setCharacterDirection("Left");
        savedQuote.setSource("simpsons");

        when(quoteRepository.findByQuoteText("Test quote"))
                .thenReturn(Optional.empty()) // First call in saveQuote
                .thenReturn(Optional.of(savedQuote)); // Second call in convertModelToQuote
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuoteAggregatorService service = new QuoteAggregatorService("dummy-key", builder, quoteRepository, voteRepository);

        Mono<Optional<Quote>> result = service.getSimpsonsQuote();
        StepVerifier.create(result)
                .expectNextMatches(optionalQuote -> optionalQuote.isPresent()
                        && "Test quote".equals(optionalQuote.get().getQuoteText())
                        && "Test character".equals(optionalQuote.get().getCharacter()))
                .verifyComplete();
    }

    @Test
    public void testGetFilteredSimpsonsQuote() {
        WebClient.Builder builder = Mockito.mock(WebClient.Builder.class);
        WebClient simpsonsClient = Mockito.mock(WebClient.class);
        WebClient ninjasClient = Mockito.mock(WebClient.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(simpsonsClient, ninjasClient);

        WebClient.RequestHeadersUriSpec simpsonsUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec simpsonsHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec simpsonsResponseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(simpsonsClient.get()).thenReturn(simpsonsUriSpec);
        when(simpsonsUriSpec.uri("/quotes?character=Homer")).thenReturn(simpsonsHeadersSpec);
        when(simpsonsHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(simpsonsHeadersSpec);
        when(simpsonsHeadersSpec.retrieve()).thenReturn(simpsonsResponseSpec);

        SimpsonsModel[] quotes = new SimpsonsModel[1];
        SimpsonsModel quote = new SimpsonsModel();
        quote.setQuote("Test quote");
        quote.setCharacter("Test character");
        quotes[0] = quote;

        when(simpsonsResponseSpec.bodyToMono(SimpsonsModel[].class))
                .thenReturn(Mono.just(quotes));

        QuoteRepository quoteRepository = Mockito.mock(QuoteRepository.class);
        VoteRepository voteRepository = Mockito.mock(VoteRepository.class);

        Quote savedQuote = new Quote();
        savedQuote.setQuoteText("Test quote");
        savedQuote.setCharacter("Test character");
        savedQuote.setSource("simpsons");

        when(quoteRepository.findByQuoteText("Test quote"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedQuote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuoteAggregatorService service = new QuoteAggregatorService("dummy-key", builder, quoteRepository, voteRepository);

        Mono<Optional<Quote>> result = service.getFilteredSimpsonsQuote("Homer");
        StepVerifier.create(result)
                .expectNextMatches(optionalQuote -> optionalQuote.isPresent()
                        && "Test quote".equals(optionalQuote.get().getQuoteText()))
                .verifyComplete();
    }

    @Test
    public void testGetNinjasQuote() {
        WebClient.Builder builder = Mockito.mock(WebClient.Builder.class);
        WebClient simpsonsClient = Mockito.mock(WebClient.class);
        WebClient ninjasClient = Mockito.mock(WebClient.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(simpsonsClient, ninjasClient);

        WebClient.RequestHeadersUriSpec ninjasUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec ninjasHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec ninjasResponseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(ninjasClient.get()).thenReturn(ninjasUriSpec);
        when(ninjasUriSpec.uri("/quotes")).thenReturn(ninjasHeadersSpec);
        when(ninjasHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(ninjasHeadersSpec);
        when(ninjasHeadersSpec.header("X-Api-Key", "dummy-key")).thenReturn(ninjasHeadersSpec);
        when(ninjasHeadersSpec.retrieve()).thenReturn(ninjasResponseSpec);

        NinjasModel[] quotes = new NinjasModel[1];
        NinjasModel ninjasQuote = new NinjasModel();
        ninjasQuote.setQuote("Ninja quote");
        quotes[0] = ninjasQuote;

        when(ninjasResponseSpec.bodyToMono(NinjasModel[].class))
                .thenReturn(Mono.just(quotes));

        QuoteRepository quoteRepository = Mockito.mock(QuoteRepository.class);
        VoteRepository voteRepository = Mockito.mock(VoteRepository.class);

        Quote savedQuote = new Quote();
        savedQuote.setQuoteText("Ninja quote");
        savedQuote.setSource("ninjas");

        when(quoteRepository.findByQuoteText("Ninja quote"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedQuote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuoteAggregatorService service = new QuoteAggregatorService("dummy-key", builder, quoteRepository, voteRepository);

        Mono<Optional<Quote>> result = service.getNinjasQuote();
        StepVerifier.create(result)
                .expectNextMatches(optionalQuote -> optionalQuote.isPresent()
                        && "Ninja quote".equals(optionalQuote.get().getQuoteText()))
                .verifyComplete();
    }
}