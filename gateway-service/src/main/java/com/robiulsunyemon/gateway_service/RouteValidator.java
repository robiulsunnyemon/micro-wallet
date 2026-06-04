package com.robiulsunyemon.gateway_service;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {


    public static final List<String> openApiEndpoints = List.of(
            "/auth/login",
            "/auth/register",
            "/eureka"
    );

    // Only GET request
    public static final List<String> publicGetEndpoints = List.of(
            "/wallets"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        boolean isOpenEndpoint = openApiEndpoints.stream().anyMatch(path::contains);
        if (isOpenEndpoint) {
            return false;
        }

        if (method == HttpMethod.GET) {
            boolean isPublicGet = publicGetEndpoints.stream().anyMatch(p -> path.equals(p) || path.equals(p + "/"));
            return !isPublicGet;
        }

        return true;
    };
}