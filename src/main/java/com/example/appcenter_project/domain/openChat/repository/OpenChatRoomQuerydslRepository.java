package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;

import java.util.List;

public interface OpenChatRoomQuerydslRepository {
    List<OpenChatRoom> findMyRooms(Long userId);
    List<OpenChatRoom> findByDormitory(String dormType);
    List<OpenChatRoom> findAllPublicRooms();
}
