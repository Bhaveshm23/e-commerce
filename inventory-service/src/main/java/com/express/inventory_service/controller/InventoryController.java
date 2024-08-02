package com.express.inventory_service.controller;

import com.express.inventory_service.dto.InventoryRequest;
import com.express.inventory_service.dto.InventoryResponse;
import com.express.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    //An order has multiple line items, each line item has an sku code.
    // To make a call for each sku-code is bad, therefore we use List of sku codes

    //Example request: http://localhost:8082/api/inventory?skuCodes=AirPods&skuCodes=BaxterMilk
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCodes){
        log.info("Received request to check stock for SKU codes: {}", skuCodes);
        return inventoryService.isInStock(skuCodes);
    }

    //Example request: http://localhost:8082/api/inventory/reduce

    @PostMapping("/reduce")
    @ResponseStatus(HttpStatus.OK)
    public void reduceStock(@RequestBody List<InventoryRequest> inventoryRequests){
        log.info("Received request to reduce inventory count: {}", inventoryRequests);
        inventoryService.reduceStock(inventoryRequests);
    }

}
