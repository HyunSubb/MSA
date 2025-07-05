package com.example.ordersystem.ordering.service;

import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.ProductDto;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDto;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderingService(OrderingRepository orderingRepository, RestTemplate restTemplate, ProductFeign productFeign, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.restTemplate = restTemplate;
        this.productFeign = productFeign;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Ordering orderCreate(OrderCreateDto orderDto , String userId){
//      product 조회 GET 요청
        String productGetUrl = "http://product-service/product/" + orderDto.getProductId();
//      (request url, request method, (HttpEntity) header + body, returnType)
//      해당 서비스에서 컨트롤러단에서 request로부터 header에 있는 값을 꺼냈었지만,
//      다른 서비스로 헤더에 값을 넣어서 요청을 보낼 때 헤더의 값이 유지가 되지 않기 때문에 다시 넣어줘서 요청을 보내줘야 한다.
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-User-Id", userId);

        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ProductDto> response = restTemplate.exchange(
                productGetUrl, HttpMethod.GET, httpEntity, ProductDto.class);

        ProductDto productDto = response.getBody();

        int quantity = orderDto.getProductCount();
        if(productDto.getStockQuantity() < quantity){
            throw new IllegalArgumentException("재고 부족");
        } else {
//          product 수정 PUT 요청
            String productPutUrl = "http://product-service/product/updatestock";
            httpHeaders.setContentType(MediaType.APPLICATION_JSON); // 요청을 보낼 떄 json으로 보내겠다는 것.
            HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(
                    ProductUpdateStockDto.builder()
                            .prouductId(orderDto.getProductId())
                            .stockQuantity(orderDto.getProductCount()).build()
                    , httpHeaders
            );
            restTemplate.exchange(productPutUrl, HttpMethod.PUT, updateEntity, Void.class);
        }

        Ordering ordering = Ordering.builder()
                .memberId(Long.parseLong(userId))
                .productId(orderDto.getProductId())
                .quantity(orderDto.getProductCount())
                .build();

        orderingRepository.save(ordering);
        return  ordering;
    }

    public Ordering orderFeignKafkaCreate(OrderCreateDto orderDto , String userId){

        ProductDto productDto = productFeign.getProductById(orderDto.getProductId(), userId);

        int quantity = orderDto.getProductCount();
        if(productDto.getStockQuantity() < quantity){
            throw new IllegalArgumentException("재고 부족");
        } else {
//            이 부분은 동기적인 방식으로 기다릴 필요가 없지 않을까?? 당연히 동기적인 방식이 안전하긴 하다.
//            이 부분은 kafka를 이용해서 비동기 처리를 한 번 해보자.
//            productFeign.updateProductStock(
//                    ProductUpdateStockDto.builder()
//                            .prouductId(orderDto.getProductId())
//                            .stockQuantity(orderDto.getProductCount()).build()
//            );
            kafkaTemplate.send("update-stock-topic",
                    ProductUpdateStockDto.builder()
                            .prouductId(orderDto.getProductId())
                            .stockQuantity(orderDto.getProductCount()).build());
        }

        Ordering ordering = Ordering.builder()
                .memberId(Long.parseLong(userId))
                .productId(orderDto.getProductId())
                .quantity(orderDto.getProductCount())
                .build();

        orderingRepository.save(ordering);
        return  ordering;
    }

}
