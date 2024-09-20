package com.express.order_picking_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name="picking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Picking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //Associated with an order
    @Column(nullable = false, unique = true)  // To ensure orderNumber is required and unique
    private String orderNumber;


    //@Enumerated(EnumType.STRING) // to store string instead of numbers for enum
    private PickingStatus pickingStatus;

    //Associated with user picking the order
    @Column(nullable = true)//when no user is assigned to the order, it can be null
    private Long userId;

    //useful for partial picks
    @ElementCollection
    @CollectionTable(name = "picking_item_quantities", joinColumns = @JoinColumn(name = "picking_id"))
    @MapKeyColumn(name = "item_id") // item as key
    @Column(name = "quantity") // quantity as value
    private Map<Long, Integer> itemQuantities = new HashMap<>(); // Map of item IDs to quantities to be picked


}

