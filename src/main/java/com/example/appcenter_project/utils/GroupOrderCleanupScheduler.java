package com.example.appcenter_project.utils;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupOrderCleanupScheduler {

    private final GroupOrderRepository groupOrderRepository;

    @Scheduled(cron = "0 0 0 * * ?")
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
