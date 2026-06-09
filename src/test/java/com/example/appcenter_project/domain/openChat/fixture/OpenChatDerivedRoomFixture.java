package com.example.appcenter_project.domain.openChat.fixture;

public class OpenChatDerivedRoomFixture {

    public static final Long PARENT_ROOM_ID = 1L;
    public static final Long DERIVED_ROOM_ID = 2L;
    public static final Long INVITATION_ID = 10L;
    public static final Long INVITER_USER_ID = 100L;
    public static final Long INVITEE_USER_ID = 200L;
    public static final Long HOST_USER_ID = 100L;
    public static final String ROOM_NAME = "테스트 파생 톡방";
    public static final String ROOM_DESCRIPTION = "테스트 설명";
    public static final int MAX_PARTICIPANTS = 5;

    /*
     * 구현 에이전트에게 필요한 클래스 및 DTO 목록:
     *
     * [신규 엔티티]
     * - OpenChatInvitation (id, roomId, inviterUserId, inviteeUserId, status, createdAt)
     *   - static factory: OpenChatInvitation.create(roomId, inviterUserId, inviteeUserId)
     *   - status 전이 메서드: accept(), reject()
     *
     * [신규 Enum]
     * - OpenChatInvitationStatus { PENDING, ACCEPTED, REJECTED }
     * - OpenChatRoomType { OPEN, DERIVED }
     *
     * [신규 DTO]
     * - RequestCreateDerivedRoomDto
     *   - Long parentRoomId (NotNull)
     *   - String name (NotBlank, Size(min=1, max=30))
     *   - String description (nullable)
     *   - Integer maxParticipants (Min(2))
     *
     * - RequestSendInvitationDto
     *   - Long inviteeUserId (NotNull)
     *
     * - ResponseOpenChatParticipantListDto
     *   - Long roomId
     *   - List<ResponseOpenChatParticipantDto> participants
     *   - Integer totalCount
     *
     * - ResponseOpenChatParticipantDto
     *   - Long userId
     *   - String nickname
     *   - String joinedAt (ISO 8601)
     *   - Boolean isHost
     *
     * - ResponseDerivedRoomCreatedDto
     *   - Long roomId
     *
     * - ResponseInvitationCreatedDto
     *   - Long invitationId
     *
     * - ResponseInvitationAcceptDto
     *   - Long roomId
     *   - String roomName
     *   - Integer currentParticipants
     *   - Integer maxParticipants
     *
     * [신규 Service]
     * - OpenChatInvitationService
     *   - ResponseDerivedRoomCreatedDto createDerivedRoom(Long requesterId, RequestCreateDerivedRoomDto dto)
     *   - ResponseInvitationCreatedDto sendInvitation(Long inviterUserId, Long roomId, RequestSendInvitationDto dto)
     *   - ResponseInvitationAcceptDto acceptInvitation(Long requesterId, Long roomId, Long invitationId)
     *   - void rejectInvitation(Long requesterId, Long roomId, Long invitationId)
     *   - ResponseOpenChatParticipantListDto getParticipants(Long requesterId, Long roomId)
     *
     * [신규 Repository]
     * - OpenChatInvitationRepository extends JpaRepository<OpenChatInvitation, Long>
     *   - Optional<OpenChatInvitation> findByIdAndInviteeUserId(Long id, Long inviteeUserId)
     *   - boolean existsByRoomIdAndInviteeUserIdAndStatus(Long roomId, Long inviteeUserId, OpenChatInvitationStatus status)
     *   - List<OpenChatInvitation> findByRoomId(Long roomId)
     *
     * [기존 엔티티 필드 추가]
     * - OpenChatRoom: roomType (OpenChatRoomType), parentRoomId (Long, nullable)
     *
     * [신규 Controller]
     * - OpenChatDerivedRoomController (POST /open-chat-rooms/derived)
     * - OpenChatInvitationController
     *     POST /open-chat-rooms/{roomId}/invitations
     *     POST /open-chat-rooms/{roomId}/invitations/{invitationId}/accept
     *     POST /open-chat-rooms/{roomId}/invitations/{invitationId}/reject
     *     GET  /open-chat-rooms/{roomId}/participants
     *
     * [에러 코드 추가 — ErrorCode enum]
     * - OPEN_CHAT_ROOM_FORBIDDEN (403)
     * - OPEN_CHAT_ROOM_NOT_FOUND (404)
     * - OPEN_CHAT_INVITATION_NOT_FOUND (404)
     * - OPEN_CHAT_ROOM_FULL (400)
     * - OPEN_CHAT_INVITATION_INVALID_TARGET (400)
     * - OPEN_CHAT_INVITATION_ALREADY_EXISTS (409)
     * - OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS (409)
     * - VALIDATION_ERROR (400)
     */
}
