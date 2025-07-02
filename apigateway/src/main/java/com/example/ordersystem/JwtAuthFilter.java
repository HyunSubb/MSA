package com.example.ordersystem;


import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

/*
    글로벌 필터는 스프링 시큐리티에 들어가 있는 의존성이 아니다. gateway 같은 경우에는 tomcat 기반 동기 엔진이 아닌
    netty 기반의 비동기 엔진으로 만들어진 프로젝트이기 때문에 그것에 좀 더 최적화 되어 있는 글로벌 필터를 사용한다.
 */

@Component
public class JwtAuthFilter implements GlobalFilter {

    @Value("${jwt.secretKey}")
    private String secretKey;

//    인증이 필요 없는 경로들
    private static final List<String> ALLOWED_PATHS = List.of(
            "/member/create",
            "/member/doLogin",
            "/member/refresh-token",
            "/product/list"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // token 검증
        System.out.println("token 검증 시작");
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        // path에는 이렇게 localhost:8080/member-service/member/doLogin 와 같이 요청을 보낸 서버의 url이 들어가 있을 거임.
        // 그러한 형식에서 /member/doLogin 까지만 추출해낸것. /member-service를 어떻게 떼어냈냐면 yml에서 떼어냈었음.
        String path = exchange.getRequest().getURI().getRawPath();
        System.out.println(path);
        // 인증이 필요 없는 경로는 필터를 통과
        if (ALLOWED_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("token 관련 예외 발생");
            }
            String token = bearerToken.substring(7);

            // token 검증 및 claims 추출
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 사용자 ID 추출
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // 헤더에 X-User-Id변수로 id값 추가 및 ROLE 추가
            // X를 붙이는 것은 custom header라는 것을 의미하는 널리 쓰이는 관례
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "ROLE_" + role) // 역할 추가
                    )
                    .build();

            // Spring Cloud Gateway는 여러 필터를 GatewayFilterChain이라는 구조로 관리
            // 다시 filter chain으로 되돌아 가는 로직.
            return chain.filter(modifiedExchange);
        } catch (IllegalArgumentException | MalformedJwtException | ExpiredJwtException | SignatureException |
                 UnsupportedJwtException e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
