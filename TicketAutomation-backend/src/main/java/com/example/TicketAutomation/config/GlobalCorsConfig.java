package com.example.TicketAutomation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return  new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://127.0.0.2:5500")
                        .allowedMethods("GET","POST","PUT","DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
