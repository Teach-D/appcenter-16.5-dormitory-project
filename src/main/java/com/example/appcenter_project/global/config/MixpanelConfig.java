package com.example.appcenter_project.global.config;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MixpanelConfig {

    @Value("${mixpanel.token}")
    private String token;

    @Bean
    public MessageBuilder mixpanelMessageBuilder() {
        return new MessageBuilder(token);
    }

    @Bean
    public MixpanelAPI mixpanelAPI() {
        return new MixpanelAPI();
    }
}
