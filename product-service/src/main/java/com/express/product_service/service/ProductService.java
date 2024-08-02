package com.express.product_service.service;

import com.express.product_service.dto.ProductRequest;
import com.express.product_service.dto.ProductResponse;
import com.express.product_service.model.Product;
import com.express.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor // for creating constructor of ProductRepository
public class ProductService {

    private final ProductRepository productRepository;
    public void createProduct(ProductRequest productRequest){
        //Builder to create a product object
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);

    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products =productRepository.findAll();
        //map each Product with ProductResponse
        List<ProductResponse> productResponses = new ArrayList<>();
        // Iterate over each product and map to ProductResponse
        for (Product product : products) {
            productResponses.add(mapToProductResponse(product));
        }

        return productResponses;

    }

    private ProductResponse mapToProductResponse(Product product) {
        // Build and return a ProductResponse object from a Product object
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

}
