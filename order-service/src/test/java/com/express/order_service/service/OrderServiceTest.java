package com.express.order_service.service;


import com.express.order_service.dto.InventoryResponse;
import com.express.order_service.dto.OrderLineItemsDto;
import com.express.order_service.dto.OrderRequest;
import com.express.order_service.model.Order;
import com.express.order_service.model.OrderStatus;
import com.express.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;


    private void webClientGetConfig(){
        // Mocking WebClient for GET requests
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);


    }

    private void webClientPostConfig(){
        // Mocking WebClient for POST requests
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getOrder_success(){
        Order mockOrder = new Order(1L, "101", new ArrayList<>(), LocalDateTime.now(), OrderStatus.UNASIGNED);

        when(orderRepository.findByOrderNumber(mockOrder.getOrderNumber())).thenReturn(Optional.of(mockOrder));

        Order order = orderService.getOrder("101");
        assertEquals("101", order.getOrderNumber());

        verify(orderRepository, times(1)).findByOrderNumber(anyString());
    }

    @Test
    void getOrder_whenOrderNotFound(){
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, ()->orderService.getOrder("101"));

        verify(orderRepository, times(1)).findByOrderNumber(anyString());
    }

    @Test
    void testPlaceOrder_allProductsInStock(){

        InventoryResponse[] inventoryResponses = new InventoryResponse[]{
                new InventoryResponse("AirPods", true, 200),
                new InventoryResponse("Iphone13", true, 120)
        };

        webClientGetConfig();
        webClientPostConfig();

        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        // Mocking POST response for inventory reduction
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        OrderLineItemsDto orderLineItem1 = new OrderLineItemsDto(1L, "AirPods", BigDecimal.valueOf(199.99), 2);
        OrderLineItemsDto orderLineItem2 = new OrderLineItemsDto(2L, "Iphone13", BigDecimal.valueOf(899.99), 3);

        OrderRequest orderRequest = new OrderRequest(List.of(orderLineItem1, orderLineItem2));

        // Mock orderRepository.save()
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the method under test
        orderService.placeOrder(orderRequest);

        // Verify that orderRepository.save() was called
        verify(orderRepository, times(1)).save(any(Order.class));

        verify(webClient, times(1)).get();
    }

    @Test
    void testPlaceOrder_whenProductOutOfStock(){
        String skuCode = "AirPods";
        InventoryResponse[] inventoryResponses = new InventoryResponse[]{
                new InventoryResponse(skuCode, false, 0)
        };

        webClientGetConfig();


        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        OrderLineItemsDto orderLineItemsDto = new OrderLineItemsDto(1L, skuCode, BigDecimal.valueOf(199.99), 2);
        OrderRequest orderRequest = new OrderRequest(List.of(orderLineItemsDto));

        assertThrows(RuntimeException.class, () -> orderService.placeOrder(orderRequest));

        verify(orderRepository, never()).save(any(Order.class));

    }


    @Test
    void testPlaceOrder_whenOneProductIsOutOfStock(){
        InventoryResponse[] inventoryResponses = new InventoryResponse[]{
                new InventoryResponse("AirPods", true, 200),
                new InventoryResponse("Iphone13", false, 0)
        };

        webClientGetConfig();
        webClientPostConfig();

        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        // Mocking POST response for inventory reduction
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        OrderLineItemsDto orderLineItem1 = new OrderLineItemsDto(1L, "AirPods", BigDecimal.valueOf(199.99), 2);
        OrderLineItemsDto orderLineItem2 = new OrderLineItemsDto(2L, "Iphone13", BigDecimal.valueOf(899.99), 3);

        OrderRequest orderRequest = new OrderRequest(List.of(orderLineItem1, orderLineItem2));

        // Mock orderRepository.save()
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the method under test
        orderService.placeOrder(orderRequest);

        // Verify that orderRepository.save() was called
        verify(orderRepository, times(1)).save(any(Order.class));

        verify(webClient, times(1)).get();
    }


}