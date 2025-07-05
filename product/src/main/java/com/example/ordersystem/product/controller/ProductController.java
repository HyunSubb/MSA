package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductUpdateStockDto;
import com.example.ordersystem.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ProductRegisterDto에서는 RequestBody 사용하지 않은 이유는
    // 폼 데이터 형식으로 데이터를 받도록 만들어놓았기 때문이다.
    @PostMapping("/create")
    public ResponseEntity<?> productCreate(ProductRegisterDto dto,
                             @RequestHeader("X-User-Id") String userId) {
        Product product = productService.productCreate(dto, userId);
        return new ResponseEntity<>(product.getId(), HttpStatus.CREATED);
    }

//    OrderingService에서 호출할 Product 조회 api
    @GetMapping("/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id,
                              @RequestHeader("X-User-Id") String userId) {
        ProductResDto productResDto = productService.productDetail(id);
//        앞쪽은 객체, 뒷쪽은 상태코드. 받는 쪽은 .getBody()를 하면 dto를 받을 수 있다.
        return new ResponseEntity<>(productResDto, HttpStatus.OK);
    }

//    OrderingService에서 사용할 Product 수정 api
    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDto productUpdateStockDto) {
        Product product = productService.updateStockQuantity(productUpdateStockDto);

        return new ResponseEntity<>(product.getId(), HttpStatus.OK);
    }

}
