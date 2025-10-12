package com.example.appcenter_project.utils;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GroupOrderNotification {

    private final GroupOrderRepository groupOrderRepository;
    private final FcmMessageService fcmMessageService;

    @Scheduled(cron = "0 */30 * * * ?")
    public void notifyLikedPostsNearDeadline() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        for (GroupOrder groupOrder : groupOrderRepository.findByDeadlineBetween(now, oneHourLater)) {
            for (GroupOrderLike groupOrderLike : groupOrder.getGroupOrderLikeList()) {
                User user = groupOrderLike.getUser();
                fcmMessageService.sendNotification(user, "좋아요한 공동구매가 곧 마감돼요.", groupOrder.getTitle());
            }

        }
    }
}
