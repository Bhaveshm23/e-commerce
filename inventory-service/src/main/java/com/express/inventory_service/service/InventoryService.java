package com.express.inventory_service.service;
import com.express.inventory_service.dto.InventoryRequest;
import com.express.inventory_service.dto.InventoryResponse;
import com.express.inventory_service.model.Inventory;
import com.express.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        return inventoryRepository.findBySkuCodeIn(skuCodes).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0) //In stock if qty >0
                                .build()
                ).toList();
    }

    public void reduceStock(List<InventoryRequest> inventoryRequests) {
        List<Inventory> inventories = new ArrayList<>();


        for (InventoryRequest request : inventoryRequests) {
            Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                    .orElseThrow(() -> new IllegalArgumentException("SKU code not found: " + request.getSkuCode()));

            int requestedQuantity = request.getQuantity();
            int inventoryQuantity = inventory.getQuantity();

            if (inventoryQuantity == 0) {
                log.info("Out of stock for SKU: {}", request.getSkuCode());
            } else if(request.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity must be non-negative for SKU: " + request.getSkuCode());
            } else if (requestedQuantity <= inventoryQuantity) {
                int updatedQuantity = inventoryQuantity - requestedQuantity;
                inventory.setQuantity(updatedQuantity);
                inventories.add(inventory);
                log.info("Reduced stock for SKU: {} by {}, new quantity: {}", request.getSkuCode(), requestedQuantity, updatedQuantity);
            } else {
                log.info("Insufficient stock for SKU: {}. Requested: {}, Available: {}", request.getSkuCode(), requestedQuantity, inventoryQuantity);
            }
        }
        if(!inventories.isEmpty()){
            inventoryRepository.saveAll(inventories);
        }
    }
}