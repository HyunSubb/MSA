package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductUpdateStockDto;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product productCreate(ProductRegisterDto dto, String userId){
        Product product = productRepository.save(dto.toEntity(Long.parseLong(userId)));
        return product;
    }

    public ProductResDto productDetail(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("product is not found")
        );

        ProductResDto productResDto = ProductResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();

        return productResDto;
    }

    public Product updateStockQuantity(ProductUpdateStockDto productUpdateStockDto) {
        Product product = productRepository.findById(productUpdateStockDto.getProuductId())
                .orElseThrow(()-> new EntityNotFoundException("product is not found"));

        product.updateStockQuantity(productUpdateStockDto.getStockQuantity());

        return product;
    }

//    카프카에서 consumer 사용할 떄는 빈주입 방식이 아닌 어노테이션으로 처리한다.
//    받아올 topic의 이름과 config에서 생성한 메서드의 이름을 기입
    @KafkaListener(topics = "update-stock-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) {
        System.out.println(message);
    }

}
