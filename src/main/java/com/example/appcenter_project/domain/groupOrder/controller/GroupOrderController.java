package com.example.appcenter_project.domain.groupOrder.controller;

import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderPopularSearch;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderSort;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.groupOrder.service.GroupOrderService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-orders")
public class GroupOrderController implements GroupOrderApiSpecification {

    private final GroupOrderService groupOrderService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestPart RequestGroupOrderDto requestGroupOrderDto, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        groupOrderService.saveGroupOrder(user.getId(), requestGroupOrderDto, images);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/{groupOrderId}/rating/{ratingScore}")
    public ResponseEntity<Void> addRating(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId, @PathVariable Float ratingScore) {
        groupOrderService.addRating(groupOrderId, ratingScore);
        return ResponseEntity.status(OK).build();
    }

    @GetMapping("/{groupOrderId}")
    public ResponseEntity<ResponseGroupOrderDetailDto> findGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(groupOrderService.findGroupOrder(user, groupOrderId, request));
    }

    @GetMapping("/searchLog")
    public ResponseEntity<List<String>> findGroupOrderSearchLog(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(groupOrderService.findGroupOrderSearchLog(user.getId()));
    }

    @GetMapping("/popular-search")
    public ResponseEntity<List<ResponseGroupOrderPopularSearch>> findGroupOrderPopularSearch() {
        return ResponseEntity.status(OK).body(groupOrderService.findGroupOrderPopularSearch());
    }

    @GetMapping("/{groupOrderId}/images")
    public ResponseEntity<List<ImageLinkDto>> findGroupOrderImages(@PathVariable Long groupOrderId, HttpServletRequest request) {
        List<ImageLinkDto> images = groupOrderService.findGroupOrderImages(groupOrderId, request);
        return ResponseEntity.ok(images);
    }

    @GetMapping
    public ResponseEntity<List<ResponseGroupOrderDto>> findGroupOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "최신순") String sort, @RequestParam(defaultValue = "전체") String type, @RequestParam(required = false) String search,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(OK).body(groupOrderService.findGroupOrders(user, GroupOrderSort.from(sort), GroupOrderType.from(type), search, request));
    }

    @PatchMapping("/{groupOrderId}/like")
    public ResponseEntity<Integer> likeGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        return ResponseEntity.status(OK).body(groupOrderService.likeGroupOrder(user.getId(), groupOrderId));
    }

    @PatchMapping("/{groupOrderId}/unlike")
    public ResponseEntity<Integer> unlikeGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        return ResponseEntity.status(OK).body(groupOrderService.unlikeGroupOrder(user.getId(), groupOrderId));
    }

    @PatchMapping("/{groupOrderId}/completion")
    public ResponseEntity<Void> completeGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        groupOrderService.completeGroupOrder(user.getId(), groupOrderId);
        return ResponseEntity.status(OK).build();
    }

    @PatchMapping("/{groupOrderId}/unCompletion")
    public ResponseEntity<Void> unCompleteGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        groupOrderService.unCompleteGroupOrder(user.getId(), groupOrderId);
        return ResponseEntity.status(OK).build();
    }

    @PutMapping(value = "/{groupOrderId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId, @Valid @RequestPart RequestGroupOrderDto requestGroupOrderDto, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        groupOrderService.updateGroupOrder(user.getId(), groupOrderId, requestGroupOrderDto, images);
        return ResponseEntity.status(ACCEPTED).build();
    }

    @DeleteMapping("/{groupOrderId}")
    public ResponseEntity<Void> deleteGroupOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId) {
        groupOrderService.deleteGroupOrder(user.getId(), groupOrderId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/image-name/{imageName}")
    public ResponseEntity<Void> deleteGroupOrderImage(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String imageName) {
        groupOrderService.deleteGroupOrderImage(user.getId(), imageName);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
