package com.express.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String orderNumber;
    @OneToMany(cascade=CascadeType.ALL)
    private List<OrderLineItems> orderLineItemsList;
    private LocalDateTime timeForPickup;
    private OrderStatus orderStatus;

}
