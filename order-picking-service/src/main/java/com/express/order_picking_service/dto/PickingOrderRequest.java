package com.express.order_picking_service.dto;

import com.express.order_service.model.OrderLineItems;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PickingOrderRequest {
    private Long id;
    private String orderNumber;
    private List<OrderLineItems> orderLineItemsList;
}
