package com.express.order_service.service;

import com.express.order_service.dto.InventoryRequest;
import com.express.order_service.dto.InventoryResponse;
import com.express.order_service.dto.OrderLineItemsDto;
import com.express.order_service.dto.OrderRequest;
import com.express.order_service.model.Order;
import com.express.order_service.model.OrderLineItems;
import com.express.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .orderLineItemsList(mapOrderLineItems(orderRequest))
                .build();

        log.info("Order Request SKU code: {}", orderRequest.getOrderLineItemsDtoList().get(0).getSkuCode());

        List<OrderLineItems> unavailbaleItems = new ArrayList<>(); //making a list of unavailable items
        List<InventoryRequest> inventoryRequests = new ArrayList<>();


        // get skucode of all the orderline items
        List<String> skuCodes = order.getOrderLineItemsList().stream().map(orderLineItems -> orderLineItems.getSkuCode()).toList();
        log.info("Checking inventory for skuCodes: {}", skuCodes.toArray());

        try{

            //Calling inventory service to check stock
            //Making a GET call to isInStock() in Inventory Controller
            //bodyToMono: takes the type of response
            InventoryResponse[] inventoryResponses = webClient.get()
                    .uri("http://localhost:8082/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build()) //passing skucode
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)//Inventory Response array
                    .block(); // to make sync request


            log.info("Received inventory responses: {}", Arrays.toString(inventoryResponses));

            for(OrderLineItemsDto item : orderRequest.getOrderLineItemsDtoList()){
                InventoryResponse inventoryItem = null;
                for (InventoryResponse response : inventoryResponses) {
                    if (response.getSkuCode().equals(item.getSkuCode())) {
                        inventoryItem = response;
                        break;
                    }
                }

                if (inventoryItem != null && inventoryItem.isInStock() && inventoryItem.getQuantity() >= item.getQuantity()) {
                    OrderLineItems orderLineItem = mapToDto(item);
                    order.getOrderLineItemsList().add(orderLineItem);
                    inventoryRequests.add(new InventoryRequest(item.getQuantity(),item.getSkuCode()));
                } else {
                    unavailbaleItems.add(mapToDto(item));
                }
            }

            if (!order.getOrderLineItemsList().isEmpty()) {
                // Reduce the inventory stock for available items
                webClient.post()
                        .uri("http://localhost:8082/api/inventory/reduce")
                        .bodyValue(inventoryRequests)
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                // Save partial order
                orderRepository.save(order);

                // Notify Picking Service
                notifyPickingService(order);

                log.info("Partial order placed successfully");
            } else {
                log.info("No items available to place order");
            }
        }catch (WebClientException exception){
            log.error("Error occured while checking inventory: " + exception.getMessage());
            throw new RuntimeException("Error occured while checking inventory:", exception);
        }catch (Exception e){
            log.error("Error response from Inventory service: " + e.getMessage());
            throw new RuntimeException("Error occured while placing  order", e);
        }

    }


    private void notifyPickingService(Order order){
        try{
            webClient.post()
                    .uri("http://localhost:8085/api/picking/create")
                    .bodyValue(order)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Order sent to Picking service successfully");
        }catch (WebClientException e){
            log.error("Failed to send to picking service", e.getMessage());
            throw new RuntimeException("Failed to notify Picking service",e);
        }
    }

    private List<OrderLineItems> mapOrderLineItems(OrderRequest orderRequest) {
        log.info("Order Request in mapOrderLineItems() sku code: {}", orderRequest.getOrderLineItemsDtoList().get(0).getSkuCode());

        List<OrderLineItems> lineItems = new ArrayList<>();
        for(OrderLineItemsDto orderLineItemsDto : orderRequest.getOrderLineItemsDtoList()){
            OrderLineItems orderLineItems = mapToDto(orderLineItemsDto);
            lineItems.add(orderLineItems);
        }
        log.info("Line item: {}", lineItems.get(0).toString());

        return lineItems;

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();

        orderLineItems.setId(orderLineItemsDto.getId());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }


    public Order getOrder(String orderNumber){
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(()-> new RuntimeException("No order found with order number"));
        return order;
    }
}
