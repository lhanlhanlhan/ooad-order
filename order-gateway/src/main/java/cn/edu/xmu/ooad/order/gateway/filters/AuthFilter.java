package cn.edu.xmu.ooad.order.gateway.filters;

import cn.edu.xmu.ooad.order.gateway.utils.JwtHelper;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关权限 authorization 字段的 token 过滤器
 *
 * @author Han Li
 * Created at 9/12/2020 11:03
 **/
public class AuthFilter implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    private final String tokenName;

    public AuthFilter(Config config) {
        this.tokenName = config.getTokenName();
    }

    /**
     * 在进入网关之前，鉴定 token 的权限过滤器
     * - 检查 JWT 是否合法及是否过期，如果过期则需要在 response 的头里换发新 JWT，如果不过期将旧的 JWT 在 response 的头中返回
     * - 判断用户的 shopid 是否与路径上的 shopid 一致 (如有) (路径为 0 可以不做这一检查)
     * - TODO 在 redis 中判断用户是否有权限访问 url, 如果不在redis中需要通过dubbo接口load用户权限
     * - 需要以 dubbo 接口访问 privilegeservice
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        return response.writeWith(Mono.empty());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public static class Config {
        private String tokenName;

        public Config() { }

        public String getTokenName() {
            return tokenName;
        }

        public void setTokenName(String tokenName) {
            this.tokenName = tokenName;
        }
    }
}
