package com.example.appcenter_project.global.analytics;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MixpanelService {

    private final MessageBuilder messageBuilder;
    private final MixpanelAPI mixpanelAPI;

    @Async("mixpanelExecutor")
    public void track(String distinctId, String eventName, JSONObject properties) {
        try {
            JSONObject message = messageBuilder.event(distinctId, eventName, properties);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(message);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel track 실패: event={}, userId={}, error={}", eventName, distinctId, e.getMessage());
        }
    }

    @Async("mixpanelExecutor")
    public void setProfile(String distinctId, JSONObject properties) {
        try {
            JSONObject message = messageBuilder.set(distinctId, properties);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(message);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel setProfile 실패: userId={}, error={}", distinctId, e.getMessage());
        }
    }

    @Async("mixpanelExecutor")
    public void setProfileOnce(String distinctId, JSONObject properties) {
        try {
            JSONObject message = messageBuilder.setOnce(distinctId, properties);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(message);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel setProfileOnce 실패: userId={}, error={}", distinctId, e.getMessage());
        }
    }

    @Async("mixpanelExecutor")
    public void incrementProfile(String distinctId, Map<String, Long> properties) {
        try {
            JSONObject message = messageBuilder.increment(distinctId, properties);
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(message);
            mixpanelAPI.deliver(delivery);
        } catch (Exception e) {
            log.warn("Mixpanel incrementProfile 실패: userId={}, error={}", distinctId, e.getMessage());
        }
    }
}
