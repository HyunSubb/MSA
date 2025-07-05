package com.example.ordersystem.ordering.service;

import com.example.ordersystem.ordering.dto.ProductDto;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// name은 Eureka에 등록된 호출할 서비스의 이름이다.
@FeignClient(name = "product-service")
public interface ProductFeign {
//    해당 어노테이션을 붙이는 순간 해당 인터페이스(빈 객체로 선언되기도 함) 안에 정의되는 메서드는
//    FeignClient 라이브러리를 사용한 HTTP 요청을 할 수 있는 객체가 된다라고 보면 된다.

//    feign 메서드를 사용할 때 매개변수로 productId를 넣어주면 어노테이션에 적은 해당 url로 Get 요청이 나간다.
//    feign 메서드에서 Header값을 넘겨주고 싶다면 @RequestHeader 어노테이션 붙여서 ㄱㄱ
    @GetMapping("/product/{productId}")
    ProductDto getProductById(@PathVariable Long productId,
                              @RequestHeader("X-User-Id") String userId);

    @PutMapping("/product/updatestock")
    void updateProductStock(@RequestBody ProductUpdateStockDto productUpdateStockDto);
}
