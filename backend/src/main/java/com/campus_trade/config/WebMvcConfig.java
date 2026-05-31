package com.campus_trade.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    private String absoluteUploadPath;

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!path.toFile().exists()) {
            path = Paths.get(System.getProperty("user.dir"), uploadDir).toAbsolutePath().normalize();
        }
        absoluteUploadPath = "file:" + path.toString().replace("\\", "/") + "/";
        System.out.println("====== 静态资源映射: /uploads/** -> " + absoluteUploadPath + " ======");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadPath);
    }
}
