package com.example.taskmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Разрешаем доступ с фронта (localhost:3000) ко всем эндпоинтам
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000") // Указываем фронтовой сервер
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Разрешаем методы
                .allowedHeaders("*"); // Разрешаем все заголовки
    }
}

