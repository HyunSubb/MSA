package com.example.ordersystem.ordering.service;

import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;

    public OrderingService(OrderingRepository orderingRepository) {
        this.orderingRepository = orderingRepository;
    }

    public Ordering orderCreate(OrderCreateDto orderDto , String userId){
//        이제 이 부분이 직접 db에서 proudct를 조회하는 것이 아닌 product 서버한테 http 요청을 보내야 한다.
        Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(()->
                new EntityNotFoundException("product is not found"));

        int quantity = orderDto.getProductCount();
        if(product.getStockQuantity() < quantity){
            throw new IllegalArgumentException("재고 부족");
        }else {
//            재고 감소 요청 또한 http 요청을 이용할 것임.
//            여기서 http 요청에 두 가지 방법이 있다. 동기와 비동기
            product.updateStockQuantity(orderDto.getProductCount());
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
