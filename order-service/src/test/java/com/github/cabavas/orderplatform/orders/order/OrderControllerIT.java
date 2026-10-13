package com.github.cabavas.orderplatform.orders.order;

import com.github.cabavas.orderplatform.orders.AbstractIntegrationTest;
import com.github.cabavas.orderplatform.orders.order.api.dto.CreateOrderRequest;
import com.github.cabavas.orderplatform.orders.order.api.dto.OrderItemRequest;
import com.github.cabavas.orderplatform.orders.order.api.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OrderControllerIT extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldCreateOrderAndReturn201WithCalculatedTotal() {
        var item1 = new OrderItemRequest(UUID.randomUUID(), "Keyboard", 1, new BigDecimal("450.00"));
        var item2 = new OrderItemRequest(UUID.randomUUID(), "Mouse", 2, new BigDecimal("150.00"));
        var request = new CreateOrderRequest(UUID.randomUUID(), List.of(item1, item2));

        ResponseEntity<OrderResponse> response = client.post()
                .uri("/api/orders")
                .body(request)
                .retrieve()
                .toEntity(OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("PENDING");
        assertThat(response.getBody().totalAmount()).isEqualByComparingTo("750.00");
        assertThat(response.getBody().items()).hasSize(2);
    }

    @Test
    void shouldFindOrderById() {
        var item = new OrderItemRequest(UUID.randomUUID(), "Monitor", 1, new BigDecimal("1200.00"));
        var request = new CreateOrderRequest(UUID.randomUUID(), List.of(item));

        OrderResponse created = client.post()
                .uri("/api/orders")
                .body(request)
                .retrieve()
                .body(OrderResponse.class);

        ResponseEntity<OrderResponse> response = client.get()
                .uri("/api/orders/{id}", created.id())
                .retrieve()
                .toEntity(OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(created.id());
        assertThat(response.getBody().items()).hasSize(1);
    }

    @Test
    void shouldReturn404WhenOrderDoesNotExist() {
        assertThatExceptionOfType(RestClientResponseException.class)
                .isThrownBy(() -> client.get()
                        .uri("/api/orders/{id}", UUID.randomUUID())
                        .retrieve()
                        .body(String.class))
                .satisfies(ex -> {
                    assertThat(ex.getStatusCode().value()).isEqualTo(404);
                    assertThat(ex.getResponseBodyAsString())
                            .contains("Order not found")
                            .contains("\"status\":404");
                });
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() {
        var request = new CreateOrderRequest(null, List.of());

        assertThatExceptionOfType(RestClientResponseException.class)
                .isThrownBy(() -> client.post()
                        .uri("/api/orders")
                        .body(request)
                        .retrieve()
                        .body(String.class))
                .satisfies(ex -> {
                    assertThat(ex.getStatusCode().value()).isEqualTo(400);
                    assertThat(ex.getResponseBodyAsString())
                            .contains("Invalid request")
                            .contains("errors");
                });
    }

    @Test
    void shouldListOrdersByCustomer() {
        UUID customerId = UUID.randomUUID();
        var item = new OrderItemRequest(UUID.randomUUID(), "Chair", 1, new BigDecimal("800.00"));

        client.post().uri("/api/orders")
                .body(new CreateOrderRequest(customerId, List.of(item)))
                .retrieve().toBodilessEntity();
        client.post().uri("/api/orders")
                .body(new CreateOrderRequest(customerId, List.of(item)))
                .retrieve().toBodilessEntity();

        ResponseEntity<OrderResponse[]> response = client.get()
                .uri("/api/orders?customerId={id}", customerId)
                .retrieve()
                .toEntity(OrderResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}