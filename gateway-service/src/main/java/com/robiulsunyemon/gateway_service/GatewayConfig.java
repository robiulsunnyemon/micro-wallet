package com.robiulsunyemon.gateway_service;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class GatewayConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        DefaultPartHttpMessageReader partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxParts(10);
        partReader.setMaxDiskUsagePerPart(10L * 1024 * 1024); // 10MB per part
        partReader.setEnableLoggingRequestDetails(true);

        MultipartHttpMessageReader multipartReader =
                new MultipartHttpMessageReader(partReader);

        configurer.defaultCodecs().multipartReader(multipartReader);
        configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024); // 50MB
    }
}