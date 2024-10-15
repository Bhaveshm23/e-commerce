package com.express.inventory_service;

import com.express.inventory_service.dto.InventoryRequest;
import com.express.inventory_service.dto.InventoryResponse;
import com.express.inventory_service.model.Inventory;
import com.express.inventory_service.repository.InventoryRepository;
import com.express.inventory_service.service.InventoryService;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceApplicationTests {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    public void isInStock_withAvailableItems_shouldReturnCorrectResponse(){
        List<String> skuCodes =  Arrays.asList("AirPods","BaxterMilk-3.25");
        List<Inventory> inventories = Arrays.asList(
                new Inventory(1L, "AirPods", 10),
                new Inventory(2L, "BaxterMilk-3.25", 0)
        );

        when(inventoryRepository.findBySkuCodeIn(skuCodes)).thenReturn(inventories);

        List<InventoryResponse> result = inventoryService.isInStock(skuCodes);

        assertEquals(2, result.size());
        assertTrue(result.get(0).isInStock());
        assertFalse(result.get(1).isInStock());
        assertEquals("AirPods", result.get(0).getSkuCode());
        assertEquals("BaxterMilk-3.25", result.get(1).getSkuCode());
    }


    @Test
    public void reduceStock_withInsufficientStock_shouldUpdateInventory(){

        Inventory inventory = new Inventory(1L, "AirPods", 5);
        InventoryRequest inventoryRequest = new InventoryRequest(2, "AirPods");

        when(inventoryRepository.findBySkuCode("AirPods")).thenReturn(Optional.of(inventory));

        inventoryService.reduceStock(List.of(inventoryRequest));

        assertEquals(3, inventory.getQuantity());
        verify(inventoryRepository, times(1)).saveAll(anyList());

    }

    @Test
    public void reduceStock_withZeroStock_shouldNotUpdateInventory(){
        Inventory inventory = new Inventory(1L, "MacBook", 0);
        InventoryRequest inventoryRequest = new InventoryRequest(3, "MacBook");

        when(inventoryRepository.findBySkuCode("MacBook")).thenReturn(Optional.of(inventory));

        inventoryService.reduceStock(List.of(inventoryRequest));

        assertEquals(0, inventory.getQuantity());
        verify(inventoryRepository, never()).saveAll(anyList());
    }

    @Test
    void reduceStock_withNonExistentSku_shouldThrowException() {

        InventoryRequest request = new InventoryRequest(1, "Iphone18");
        when(inventoryRepository.findBySkuCode("Iphone18")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inventoryService.reduceStock(List.of(request)));
    }

    @Test
    void reduceStock_withMultipleItems_shouldUpdateCorrectly(){
        List<InventoryRequest> requests = Arrays.asList(
            new InventoryRequest(1, "AirPods"),
                new InventoryRequest(1, "Iphone16")
        );

        Inventory airPodsInventory = new Inventory(1L,"AirPods", 40);
        Inventory iphone16Inventory = new Inventory(2L,"Iphone16", 10);

        when(inventoryRepository.findBySkuCode("AirPods")).thenReturn(Optional.of(airPodsInventory));
        when(inventoryRepository.findBySkuCode("Iphone16")).thenReturn(Optional.of(iphone16Inventory));

        inventoryService.reduceStock(requests);

        assertEquals(39,airPodsInventory.getQuantity());
        assertEquals(9, iphone16Inventory.getQuantity());
        verify(inventoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void reduceStock_withNegativeQuantity_shouldThrowException() {
        InventoryRequest request = new InventoryRequest(-1, "AirPods");

        assertThrows(IllegalArgumentException.class, () -> inventoryService.reduceStock(List.of(request)));
    }

}
