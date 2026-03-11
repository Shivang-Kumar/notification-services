package com.practice.microservices.notification_service.services;


import org.springframework.stereotype.Service;
import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.RenderedTemplateDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationProcessor {

    private final TemplateService templateService;
    private final ChannelRouter channelRouter;

    public void process(Notification notification) throws Exception {

        // 1 Render template
        RenderedTemplateDto renderedTemplate =
                templateService.renderTemplate(notification);

        // 2 Route to correct channel
        channelRouter.route(
                notification,
                renderedTemplate.getSubject(),
                renderedTemplate.getBody()
        );
    }
}