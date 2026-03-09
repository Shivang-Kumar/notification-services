package com.practice.microservices.notification_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeleafConfig {
@Bean
    public TemplateEngine notificationTemplateEngine() {

        TemplateEngine engine = new TemplateEngine();

        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false);

        engine.setTemplateResolver(resolver);

        return engine;
    }
}
