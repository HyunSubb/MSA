package com.example.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data // getter, setter, toString 자동 추가
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
}
