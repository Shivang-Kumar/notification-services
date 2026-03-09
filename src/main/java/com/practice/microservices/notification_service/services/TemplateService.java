package com.practice.microservices.notification_service.services;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationTemplate;
import com.practice.microservices.notification_service.models.NotificationTemplateDto;
import com.practice.microservices.notification_service.models.RenderedTemplateDto;
import com.practice.microservices.notification_service.repository.NotificationTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateService {
	
	private final NotificationTemplateRepository templateRepository;
	private final TemplateEngine templateEngine;
	private final ObjectMapper objectMapper;
	
	public NotificationTemplate createTemplate(NotificationTemplateDto request) {

	    NotificationTemplate template = new NotificationTemplate();

	    template.setTemplateCode(request.getTemplateCode());
	    template.setChannel(request.getChannel());
	    template.setSubject(request.getSubject());
	    template.setBody(request.getBody());
	    template.setActive(request.isActive());
	    template.setVersion(request.getVersion());

	    return templateRepository.save(template);
	}
	
	
	public RenderedTemplateDto renderTemplate(Notification notification)
	{
		NotificationTemplate template=templateRepository.findByTemplateCodeAndActiveTrue(notification.getTemplateId())
				.orElseThrow(()-> new RuntimeException("Template not found"+notification.getTemplateId()));
		
		try {
			Map<String,Object> payloadMap=objectMapper.readValue(notification.getPayload(), new TypeReference<Map<String,Object>>(){});
			Context context=new Context();
			context.setVariables(payloadMap);
			String renderedSubject=templateEngine.process(template.getSubject(),context);
			String renderedBody=templateEngine.process(template.getBody(), context);
			return new RenderedTemplateDto(renderedSubject, renderedBody);
		}
		catch(Exception e){
			throw new RuntimeException("Failed to render template",e);
			
		}
	}
	
}
