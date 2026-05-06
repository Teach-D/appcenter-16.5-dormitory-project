package com.example.appcenter_project.domain.calender.controller;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.ScheduleExtractStatus;
import com.example.appcenter_project.domain.announcement.repository.CrawledAnnouncementRepository;
import com.example.appcenter_project.domain.calender.dto.response.ResponseFailedScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/ai-schedule")
@RequiredArgsConstructor
public class AiScheduleAdminController implements AiScheduleAdminApiSpecification {

    private static final int RETRY_LIMIT = 3;

    private final CrawledAnnouncementRepository crawledAnnouncementRepository;

    @Override
    @GetMapping("/failed")
    public List<ResponseFailedScheduleDto> getFailedList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<CrawledAnnouncement> result = crawledAnnouncementRepository
                .findByScheduleExtractStatusAndScheduleExtractRetryCountGreaterThanEqual(
                        ScheduleExtractStatus.FAILED, RETRY_LIMIT, PageRequest.of(page, size));

        return result.stream()
                .map(ResponseFailedScheduleDto::from)
                .toList();
    }
}