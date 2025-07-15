package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.cache.GroupOrderCacheDto1;
import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.GroupOrderImageDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.groupOrder.DeliveryCacheService;
import com.example.appcenter_project.service.groupOrder.GroupOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-orders")
public class GroupOrderController {

    private final GroupOrderService groupOrderService;
    private final DeliveryCacheService deliveryCacheService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestPart RequestGroupOrderDto requestGroupOrderDto, @RequestPart List<MultipartFile> images) {
        groupOrderService.saveGroupOrder(user.getId(), requestGroupOrderDto, images);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/cache/{id}")
    public ResponseEntity<GroupOrderCacheDto1> getCacheDelivery(@PathVariable Long id) {
        GroupOrderCacheDto1 groupOrderCacheById = groupOrderService.findGroupOrderCacheById(id);
        return ResponseEntity.status(OK).body(groupOrderCacheById);
    }

    @GetMapping("/{groupOrderId}")
    public ResponseEntity<ResponseGroupOrderDetailDto> findGroupOrderById(@PathVariable Long groupOrderId) {
        return ResponseEntity.status(OK).body(groupOrderService.findGroupOrderById(groupOrderId));
    }

    @GetMapping("/{groupOrderId}/images")
    public ResponseEntity<List<GroupOrderImageDto>> getGroupOrderImages(@PathVariable Long groupOrderId) {
        List<GroupOrderImageDto> images = groupOrderService.findGroupOrderImages(groupOrderId);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/searchLog")
    public ResponseEntity<List<String>> findGroupOrderSearchLog(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(groupOrderService.findGroupOrderSearchLog(user.getId()));
    }

    @GetMapping("/images/view")
    public ResponseEntity<Resource> viewImage(@RequestParam String filename) {
        Resource resource = groupOrderService.loadImageAsResource(filename);
        File file = new File(resource.getFilename());
        String contentType = groupOrderService.getImageContentType(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<ResponseGroupOrderDto>> findGroupOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "DEADLINE") String sort, @RequestParam(defaultValue = "ALL") String type, @RequestParam(required = false) Optional<String> search
    ) {
        return ResponseEntity.status(OK).body(groupOrderService.findGroupOrders(user.getId(), GroupOrderSort.from(sort), GroupOrderType.from(type), search));
    }

    @PatchMapping("/{groupOrderId}/like")
    public ResponseEntity<Integer> likePlusGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        return ResponseEntity.status(OK).body(groupOrderService.likePlusGroupOrder(user.getId(), groupOrderId));
    }

    @PatchMapping("/{groupOrderId}/unlike")
    public ResponseEntity<Integer> likeMinusGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        return ResponseEntity.status(OK).body(groupOrderService.likeMinusGroupOrder(user.getId(), groupOrderId));
    }

    @PutMapping("/{groupOrderId}")
    public ResponseEntity<ResponseGroupOrderDetailDto> updateGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId, @Valid @RequestBody RequestGroupOrderDto requestGroupOrderDto) {
        return ResponseEntity.status(ACCEPTED).body(groupOrderService.updateGroupOrder(user.getId(), groupOrderId, requestGroupOrderDto));
    }

    @DeleteMapping("/{groupOrderId}")
    public ResponseEntity<Void> deleteGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        groupOrderService.deleteGroupOrder(user.getId(), groupOrderId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
