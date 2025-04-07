package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.request.user.RequestUserDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.groupOrder.GroupOrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-orders")
public class GroupOrderController {

    private final GroupOrderService groupOrderService;

    @PostMapping
    public ResponseEntity<Void> saveGroupOrder(@AuthenticationPrincipal SecurityUser user, @RequestBody RequestGroupOrderDto requestGroupOrderDto) {
        groupOrderService.saveGroupOrder(user.getId(), requestGroupOrderDto);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/{groupOrderId}")
    public ResponseEntity<ResponseGroupOrderDto> findGroupOrderById(@PathVariable Long groupOrderId) {
        return ResponseEntity.status(FOUND).body(groupOrderService.findGroupOrderById(groupOrderId));
    }

    @GetMapping
    public ResponseEntity<List<ResponseGroupOrderDto>> findGroupOrders(
            @RequestParam(defaultValue = "DEADLINE") String sort, @RequestParam(defaultValue = "ALL") String type, @RequestParam(required = false) Optional<String> search
    ) {
        return ResponseEntity.status(FOUND).body(groupOrderService.findGroupOrders(GroupOrderSort.valueOf(sort), GroupOrderType.valueOf(type), search));
    }

    @PatchMapping("/like/{groupOrderId}")
    public ResponseEntity<Integer> likePlusGroupOrder(@AuthenticationPrincipal SecurityUser user, @PathVariable Long groupOrderId) {
        return ResponseEntity.status(OK).body(groupOrderService.likePlusGroupOrder(user.getId(), groupOrderId));
    }

    @PutMapping("/{groupOrderId}")
    public ResponseEntity<ResponseGroupOrderDto> updateGroupOrder(@PathVariable Long groupOrderId, @RequestBody RequestGroupOrderDto requestGroupOrderDto) {
        return ResponseEntity.status(FOUND).body(groupOrderService.updateGroupOrder(groupOrderId, requestGroupOrderDto));
    }

    @DeleteMapping("/{groupOrderId}")
    public ResponseEntity<Void> deleteGroupOrder(@PathVariable Long groupOrderId) {
        groupOrderService.deleteGroupOrder(groupOrderId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}