package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEWS_URL = "/v1/reviews";

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {

                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });

        //then
    }

    @Test
    void addReview_validation() {
        //given
        var review = new Review(null, null, "Awesome Movie", -9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest();

        //then
    }

    @Test
    void getAllReview() {
        //given
        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 1L, "Excellent Movie", 8.0));

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {
        //given
        var reviewId = "abc";
        var review = new Review(null, 1L, "Super Awesome Movie", 9.5);

        when(reviewReactiveRepository.findById(isA(String.class))).thenReturn(
                Mono.just(new Review(reviewId, 1L, "Super Awesome Movie", 9.5))
        );
        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(
                Mono.just(new Review(reviewId, 1L, "Super Awesome Movie", 9.5))
        );

        //when
        webTestClient
                .put()
                .uri(REVIEWS_URL+"/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {

                    var updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    assert updatedReview.getReviewId() != null;
                    assertEquals(1L, updatedReview.getMovieInfoId());
                    assertEquals("Super Awesome Movie", updatedReview.getComment());
                    assertEquals(9.5, updatedReview.getRating());
                });
    }

    @Test
    void deleteReview() {
        var reviewId = "abc";

        when(reviewReactiveRepository.findById(isA(String.class))).thenReturn(
                Mono.just(new Review(reviewId, 1L, "Awesome Movie", 9.0))
        );

        when(reviewReactiveRepository.deleteById(isA(String.class))).thenReturn(
                Mono.empty()
        );

        //when
        webTestClient
                .delete()
                .uri(REVIEWS_URL+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .isEmpty();
    }
}
