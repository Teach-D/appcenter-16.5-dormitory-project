package com.example.appcenter_project.global.scheduler;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupOrderCleanupScheduler {

    private final GroupOrderRepository groupOrderRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final FcmMessageService fcmMessageService;

    @Scheduled(cron = "0 0 8-23 * * *")
    @Transactional
    public void findGroupOrdersEndingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<GroupOrder> endingSoonOrders = groupOrderRepository.findGroupOrdersEndingSoon(now, oneHourLater);

        for (GroupOrder groupOrder : endingSoonOrders) {
            if (groupOrder.getGroupOrderLikeList() == null || groupOrder.getGroupOrderLikeList().isEmpty()) {
                continue;
            }

            Notification notification = Notification.builder()
                    .boardId(groupOrder.getId())
                    .title("좋아요한 공동구매가 곧 마감돼요!")
                    .body(groupOrder.getTitle())
                    .notificationType(NotificationType.GROUP_ORDER)
                    .apiType(ApiType.GROUP_ORDER)
                    .build();

            notificationRepository.save(notification);

            for (GroupOrderLike groupOrderLike : groupOrder.getGroupOrderLikeList()) {
                User user = groupOrderLike.getUser();

                if (user.getReceiveNotificationTypes().contains(NotificationType.GROUP_ORDER)) {
                    UserNotification userNotification = UserNotification.of(user, notification);
                    userNotificationRepository.save(userNotification);

                    fcmMessageService.sendNotification(user, notification.getTitle(), notification.getBody());
                }


            }

        }

    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void deleteGroupOrder() {
        LocalDate now = LocalDate.now();
        List<GroupOrder> groupOrders = groupOrderRepository.findAll();

        List<GroupOrder> deliveryGroupOrders = groupOrders.stream().filter(groupOrder -> {
            return groupOrder.getGroupOrderType().equals(GroupOrderType.DELIVERY);
        }).toList();

        List<GroupOrder> groceryGroupOrders = groupOrders.stream().filter(groupOrder -> {
            return groupOrder.getGroupOrderType().equals(GroupOrderType.GROCERY);
        }).toList();

        List<GroupOrder> lifeItemGroupOrders = groupOrders.stream().filter(groupOrder -> {
            return groupOrder.getGroupOrderType().equals(GroupOrderType.LIFE_ITEM);
        }).toList();

        List<GroupOrder> etcGroupOrders = groupOrders.stream().filter(groupOrder -> {
            return groupOrder.getGroupOrderType().equals(GroupOrderType.ETC);
        }).toList();

        deleteDeliveryGroupOrders(deliveryGroupOrders, now);
        deleteGroceryGroupOrders(groceryGroupOrders, now);
        deleteLifeItemGroupOrders(lifeItemGroupOrders, now);
        deleteEtcGroupOrders(etcGroupOrders, now);

        // 마감일이 지난 GroupOrder 삭제
        deleteAfterDeadlineGroupOrder();
    }

    private void deleteAfterDeadlineGroupOrder() {
        LocalDate now = LocalDate.now();

        for (GroupOrder groupOrder : groupOrderRepository.findAll()) {
            if (now.isAfter(groupOrder.getDeadline().toLocalDate())) {
                groupOrderRepository.delete(groupOrder);
            }
        }
    }

    private void deleteDeliveryGroupOrders(List<GroupOrder> deliveryGroupOrders, LocalDate now) {
        for (GroupOrder deliveryGroupOrder : deliveryGroupOrders) {
            LocalDate createdDate = deliveryGroupOrder.getCreatedDate().toLocalDate();

            if (now.isAfter(createdDate)) {
                groupOrderRepository.delete(deliveryGroupOrder);
            }

        }
    }

    private void deleteGroceryGroupOrders(List<GroupOrder> groceryGroupOrders, LocalDate now) {
        for (GroupOrder groceryGroupOrder : groceryGroupOrders) {
            LocalDate createdDate = groceryGroupOrder.getCreatedDate().toLocalDate();

            if (now.isAfter(createdDate.plusDays(7))) {
                groupOrderRepository.delete(groceryGroupOrder);
            }
        }
    }

    private void deleteLifeItemGroupOrders(List<GroupOrder> lifeItemGroupOrders, LocalDate now) {
        for (GroupOrder lifeItemGroupOrder : lifeItemGroupOrders) {
            LocalDate createdDate = lifeItemGroupOrder.getCreatedDate().toLocalDate();

            if (now.isAfter(createdDate.plusDays(7))) {
                groupOrderRepository.delete(lifeItemGroupOrder);
            }
        }
    }

    private void deleteEtcGroupOrders(List<GroupOrder> etcGroupOrders, LocalDate now) {
        for (GroupOrder etcGroupOrder : etcGroupOrders) {
            LocalDate createdDate = etcGroupOrder.getCreatedDate().toLocalDate();

            if (now.isAfter(createdDate.plusDays(7))) {
                groupOrderRepository.delete(etcGroupOrder);
            }
        }
    }
}
