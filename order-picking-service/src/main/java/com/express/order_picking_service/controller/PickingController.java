package com.express.order_picking_service.controller;


import com.express.order_picking_service.dto.PickingOrderRequest;
import com.express.order_picking_service.model.Picking;
import com.express.order_picking_service.service.PickingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/picking")
@RequiredArgsConstructor
public class PickingController {

    private final PickingService pickingService;

    //unassign
    //picking
    //stage -> partially staged/staged

    //Create a Picking record when an order is placed
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Picking createPickingRecord(@RequestBody PickingOrderRequest order){
        return pickingService.createPickingRecord(order);
    }


    //assign order to a user for picking
    //Example: http://localhost:8080/api/picking/assign?orderNumber=123&userId=48
    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.OK)
    public void assignOrder(@RequestParam String orderNumber, @RequestParam Long userId){
        pickingService.assignOrderToUser(orderNumber, userId);
    }

    //Get the status of picking
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public String getOrderStatus(@RequestParam Long pickingId){
        return pickingService.getOrderStatus(pickingId);
    }

    @PostMapping("/item-pick")
    @ResponseStatus(HttpStatus.OK)
    public void pickItem(@RequestParam String orderNumber, @RequestParam Long itemId, @RequestParam Integer pickedQuantity){
        pickingService.pickItems(orderNumber, itemId, pickedQuantity);
    }

    @GetMapping("/stage")
    @ResponseStatus(HttpStatus.OK)
    public void stageOrder(@RequestParam String orderNumber){
        pickingService.stageOrder(orderNumber);
    }

}
