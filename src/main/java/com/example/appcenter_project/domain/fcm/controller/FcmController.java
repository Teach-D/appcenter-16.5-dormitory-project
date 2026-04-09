package com.example.appcenter_project.domain.fcm.controller;

import com.example.appcenter_project.common.metrics.annotation.TrackApi;
import com.example.appcenter_project.domain.fcm.dto.request.RequestFcmMessageDto;
import com.example.appcenter_project.domain.fcm.dto.request.RequestFcmTokenDto;
import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmDlqDto;
import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmMessageDto;
import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmStatsDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.fcm.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController implements FcmApiSpecification{

    private final FcmTokenService fcmTokenService;
    private final FcmMessageService fcmMessageService;

    @PostMapping("/token")
    public ResponseEntity<Void> saveToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestFcmTokenDto requestDto
    ) {
        fcmTokenService.saveToken(userDetails, requestDto.getFcmToken());
        return ResponseEntity.ok().build();
    }

    @TrackApi
    @GetMapping("/stats")
    public ResponseEntity<ResponseFcmStatsDto> getFcmStats() {
        return ResponseEntity.ok(fcmMessageService.getFcmStats());
    }

    // 전체 사용자에게 알림 전송 (user_id가 NULL인 토큰도 포함)
    @PostMapping("/send/all")
    public ResponseEntity<ResponseFcmMessageDto> sendMessageToAllUsers(
            @RequestBody RequestFcmMessageDto requestDto
    ) {
        ResponseFcmMessageDto response = fcmMessageService.sendNotificationToAllUsers(
                requestDto.getTitle(),
                requestDto.getBody()
        );
        return ResponseEntity.ok(response);
    }

    // FcmController.java — 테스트 후 삭제
    @PostMapping("/send-all/benchmark")
    public ResponseEntity<Map<String, Object>> benchmark(
            @RequestBody RequestFcmMessageDto dto) throws InterruptedException {

        com.sun.management.OperatingSystemMXBean osMXBean =
                (com.sun.management.OperatingSystemMXBean)
                        ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        // 측정 전 안정화
        Thread.sleep(200);
        long heapBefore   = memoryMXBean.getHeapMemoryUsage().getUsed() / 1024 / 1024;
        long sysTotalMem  = osMXBean.getTotalMemorySize() / 1024 / 1024;
        long sysFreeMemBefore = osMXBean.getFreeMemorySize() / 1024 / 1024;

        // 백그라운드 CPU + 메모리 샘플링 (200ms 간격)
        List<Double> cpuSamples  = new ArrayList<>();
        List<Long>   heapSamples = new ArrayList<>();
        List<Long>   sysSamples  = new ArrayList<>();
        AtomicBoolean sampling   = new AtomicBoolean(true);
        Thread sampler = new Thread(() -> {
            while (sampling.get()) {
                double cpu = osMXBean.getProcessCpuLoad() * 100;
                if (cpu >= 0) cpuSamples.add(cpu);
                heapSamples.add(memoryMXBean.getHeapMemoryUsage().getUsed() / 1024 / 1024);
                sysSamples.add((sysTotalMem - osMXBean.getFreeMemorySize() / 1024 / 1024));
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        sampler.setDaemon(true);
        sampler.start();

        long start = System.currentTimeMillis();

        // FCM 전송
        fcmMessageService.sendNotificationToAllUsers(dto.getTitle(), dto.getBody());

        long elapsed = System.currentTimeMillis() - start;

        // 샘플링 중단
        sampling.set(false);
        sampler.join(500);

        long heapAfter       = memoryMXBean.getHeapMemoryUsage().getUsed() / 1024 / 1024;
        long sysFreeMemAfter = osMXBean.getFreeMemorySize() / 1024 / 1024;

        double cpuMax  = cpuSamples.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double cpuAvg  = cpuSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        long heapMax   = heapSamples.stream().mapToLong(Long::longValue).max().orElse(0);
        long heapAvg   = (long) heapSamples.stream().mapToLong(Long::longValue).average().orElse(0);
        long sysMax    = sysSamples.stream().mapToLong(Long::longValue).max().orElse(0);
        long sysAvg    = (long) sysSamples.stream().mapToLong(Long::longValue).average().orElse(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("elapsedMs",           elapsed);
        result.put("cpu_max_%",           String.format("%.2f", cpuMax));
        result.put("cpu_avg_%",           String.format("%.2f", cpuAvg));
        result.put("samples",             cpuSamples.size());
        result.put("heap_max_MB",         heapMax);
        result.put("heap_avg_MB",         heapAvg);
        result.put("heap_before_MB",      heapBefore);
        result.put("heap_after_MB",       heapAfter);
        result.put("heap_delta_MB",       heapAfter - heapBefore);
        result.put("sys_total_MB",        sysTotalMem);
        result.put("sys_used_max_MB",     sysMax);
        result.put("sys_used_avg_MB",     sysAvg);
        result.put("sys_used_before_MB",  sysTotalMem - sysFreeMemBefore);
        result.put("sys_used_after_MB",   sysTotalMem - sysFreeMemAfter);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/dlq")
    public ResponseEntity<Page<ResponseFcmDlqDto>> getDlqList(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(fcmMessageService.getDlqList(pageable));
    }

    @PostMapping("/dlq/{outboxId}/retry")
    public ResponseEntity<Void> retryDlq(@PathVariable Long outboxId) {
        fcmMessageService.retryDlq(outboxId);
        return ResponseEntity.ok().build();
    }

/*    @PostMapping("/send")
    public ResponseEntity<ResponseFcmMessageDto> sendMessage(
            @RequestBody RequestFcmMessageDto requestDto
    ) {
        String messageId = fcmMessageService.sendNotification(
                requestDto.getTargetToken(),
                requestDto.getTitle(),
                requestDto.getBody()
        );

        return ResponseEntity.ok(
                ResponseFcmMessageDto.builder()
                        .messageId(messageId)
                        .status("SUCCESS")
                        .build()
        );
    }*/
}
