package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;

import java.util.List;

public interface OpenChatRoomQuerydslRepository {
    List<OpenChatRoom> findMyRooms(Long userId, String keyword);
    List<OpenChatRoom> findByDormitory(String dormType, String keyword);
    List<OpenChatRoom> findAllPublicRooms(String keyword);
}
