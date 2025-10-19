package org.example.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")   // 允许任何源
                .allowedMethods("*")          // 允许任何方法
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}