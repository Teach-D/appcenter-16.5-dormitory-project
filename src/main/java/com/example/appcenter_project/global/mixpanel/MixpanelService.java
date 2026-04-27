package com.example.appcenter_project.global.mixpanel;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MixpanelService {

    private final MessageBuilder messageBuilder;
    private final MixpanelAPI mixpanelAPI;

    public MixpanelService(@Value("${mixpanel.token}") String token) {
        this.messageBuilder = new MessageBuilder(token);
        this.mixpanelAPI = new MixpanelAPI();
    }

    @Async("mixpanelExecutor")
    public void trackEvent(String distinctId, String eventName, JSONObject properties) {
        try {
            JSONObject event = messageBuilder.event(distinctId, eventName, properties);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(event);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel trackEvent 실패 - distinctId: {}, event: {}, error: {}", distinctId, eventName, e.getMessage());
        }
    }

    @Async("mixpanelExecutor")
    public void setUserProfile(String distinctId, JSONObject properties) {
        try {
            JSONObject update = messageBuilder.set(distinctId, properties);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(update);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel setUserProfile 실패 - distinctId: {}, error: {}", distinctId, e.getMessage());
        }
    }

    @Async("mixpanelExecutor")
    public void incrementUserProperty(String distinctId, String property, long value) {
        try {
            java.util.Map<String, Long> incrementProps = new java.util.HashMap<>();
            incrementProps.put(property, value);
            JSONObject update = messageBuilder.increment(distinctId, incrementProps);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(update);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel incrementUserProperty 실패 - distinctId: {}, property: {}, error: {}", distinctId, property, e.getMessage());
        }
    }

    @Async("mixpanelExecutor")
    public void identifyUser(String anonymousId, String userId) {
        try {
            JSONObject props = new JSONObject();
            props.put("$anon_distinct_id", anonymousId);
            JSONObject event = messageBuilder.event(userId, "$identify", props);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(event);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel identifyUser 실패 - anonymousId: {}, userId: {}, error: {}", anonymousId, userId, e.getMessage());
        }
    }
}
