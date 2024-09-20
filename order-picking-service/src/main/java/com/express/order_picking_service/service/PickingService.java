package com.express.order_picking_service.service;


import com.express.order_picking_service.dto.OrderLineItemsDto;
import com.express.order_picking_service.dto.OrderResponse;
import com.express.order_picking_service.dto.PickingOrderRequest;
import com.express.order_picking_service.model.PickingStatus;
import com.express.order_picking_service.repository.PickingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.express.order_picking_service.model.Picking;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PickingService {


    private final PickingRepository pickingRepository;
    private final WebClient webClient;


    // To show new order on the front-end dashboard
    public Picking createPickingRecord(PickingOrderRequest pickingOrderRequest){
        //create a new picking record
        Picking picking = pickingRepository.findByOrderNumber(pickingOrderRequest.getOrderNumber()).orElse(new Picking());


        //check if order is null, then not found
        if(pickingOrderRequest == null){
            throw new RuntimeException("Order not found in the Order Service!!");
        }
        picking.setOrderNumber(pickingOrderRequest.getOrderNumber());
        picking.setPickingStatus(PickingStatus.UNASIGNED);

        UUID uuid = UUID.randomUUID();
        Long pickingId = uuid.getMostSignificantBits();

        picking.setId(pickingId);

        //Intially not picking
        picking.setUserId(null);
        picking.setItemQuantities(new HashMap<>());

        log.info("OrderNumber :{}", picking.getOrderNumber());

        pickingRepository.save(picking);
        return picking;
    }


 /*
        User will pick the items. When PICK button is clicked in the front-end, it will do a front-end check
        if the picked quantity is equal to the ordered quantity. If not it will show an error.
        At last, when the stage button is clicked, it will compare the picked quantity and ordered quantity
        if picked qty < ordered qty change status to PARTAIALLY_STAGED
        else picked qty == ordered qty change status to STAGED

     */

    @Transactional //To make sure its a transaction and at a time single user picks order
    public void assignOrderToUser(String orderNumber, Long userId){

        //Find existing picking record
        Picking picking = pickingRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new RuntimeException("Picking record not found!!"));

        //Check if picking is already assigned to another user
        if(picking.getUserId()!=null && !picking.getUserId().equals(userId)){
            throw new RuntimeException("This order is already being picked by user: " + picking.getUserId());
        }

        // Update Picking details with order information and assign user
        picking.setPickingStatus(PickingStatus.PICKING);
        picking.setUserId(userId);

        pickingRepository.save(picking);
            log.info("Order assigned to user: {}", userId);
            log.info("Picking Item Qts :{}", picking.getItemQuantities());
    }


    //Item Picking
    public void pickItems(String orderNumber, Long itemId, Integer pickedQuantity){
        //Find existing picking record
        Picking picking = pickingRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new RuntimeException("Picking record not found!!"));

        Map<Long, Integer> itemQuantitiesToBePicked = picking.getItemQuantities();

        // If the item is already in the map, update the quantity
        itemQuantitiesToBePicked.put(itemId, itemQuantitiesToBePicked.getOrDefault(itemId, 0) + pickedQuantity);


        // Save the updated picking record
        pickingRepository.save(picking);
        log.info("Updated picking record for order: {}, item: {}, picked qty: {}", orderNumber, itemId, pickedQuantity);

    }

    //Stage order
    public void stageOrder(String orderNumber){

        Picking picking = pickingRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new RuntimeException("Picking record not found!!"));

        Map<Long, Integer> itemQuantitiesToBePicked = picking.getItemQuantities();

        OrderResponse orderResponse = fetchOrderDetails(orderNumber);

        List<OrderLineItemsDto> orderLineItemsDtoList =  orderResponse.getOrderLineItemsDtoList();

        boolean allItemsPicked = true;

        // Compare picked quantities with ordered quantities
        for (OrderLineItemsDto orderItem : orderLineItemsDtoList) {
            Integer orderedQuantity = orderItem.getQuantity();
            Long orderItemId = orderItem.getId();

            // Get picked quantity for the corresponding item
            Integer currentPickedQuantity = itemQuantitiesToBePicked.getOrDefault(orderItemId, 0);

            // If any item is not fully picked, set allItemsPicked to false
            if (currentPickedQuantity < orderedQuantity) {
                allItemsPicked = false;
            }
        }

        // Update the picking status based on whether all items are picked
        if (allItemsPicked) {
            picking.setPickingStatus(PickingStatus.STAGED);
        } else {
            picking.setPickingStatus(PickingStatus.PARTIALLY_STAGED);
        }

        // Save the updated picking record with status
        pickingRepository.save(picking);

        log.info("Updated picking status for order: {} to {}", orderNumber, picking.getPickingStatus());

    }

   public String getOrderStatus(Long pickingId){
        Picking picking = pickingRepository.findById(pickingId)
                .orElseThrow(()->new RuntimeException("Picking record not found"));

        return String.valueOf(picking.getPickingStatus());

    }

    private OrderResponse fetchOrderDetails(String orderNumber){
        OrderResponse  orderResponse = webClient.get()
                .uri("http://localhost:8081/api/order",
                        uriBuilder -> uriBuilder.queryParam("orderNumber", orderNumber).build()) //passing skucode
                .retrieve()
                .bodyToMono(OrderResponse.class)//Inventory Response array
                .block(); // to make sync request

        log.info("Order Response from fetchOrderDetails: {}", orderResponse);
        return orderResponse;
    }



}
