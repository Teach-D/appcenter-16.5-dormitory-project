package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.repository.groupOrder.GroupOrderCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderCommentService {

    private final GroupOrderCommentRepository groupOrderCommentRepository;
}
