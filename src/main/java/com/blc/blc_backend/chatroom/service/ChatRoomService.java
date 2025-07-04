package com.blc.blc_backend.chatroom.service;

import com.blc.blc_backend.chatroom.dto.ChatRoomDetailResponse;
import com.blc.blc_backend.chatroom.dto.ChatRoomRequest;
import com.blc.blc_backend.chatroom.dto.ChatRoomResponse;
import com.blc.blc_backend.chatroom.entity.ChatRoom;
import com.blc.blc_backend.chatroom.repository.ChatRoomRepository;
import com.blc.blc_backend.game.dto.GameDetailInfo;
import com.blc.blc_backend.game.entity.Game;
import com.blc.blc_backend.game.repository.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final GameRepository gameRepository;

    // 1) 채팅방 생성
    @Transactional
    public ChatRoomResponse create(Long gameId, String roomName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));

        // 이미 같은 경기로 채팅방이 있을 경우 예외 처리하거나 갱신할 수 있음
        if (chatRoomRepository.findByGame_GameId(gameId).isPresent()) {
            throw new IllegalStateException("Chat room for this game already exists");
        }

        ChatRoom room = ChatRoom.builder()
                .game(game)
                .roomName(roomName)
                .isActive(true)
                .maxParticipants(10000)
                .build();

        ChatRoom saved = chatRoomRepository.save(room);
        return ChatRoomResponse.of(saved);
    }

    // 추가: 고정 채팅방 생성 메서드
    @Transactional
    public ChatRoomResponse createGeneralChatRoom(String roomName) {
        // 이미 고정 채팅방이 있는지 확인
        Optional<ChatRoom> existing = chatRoomRepository.findGeneralChatRoom();
        if (existing.isPresent()) {
            throw new IllegalStateException("General chat room already exists");
        }

        ChatRoom room = ChatRoom.builder()
                .game(null)  // 고정 채팅방은 game이 null
                .roomName(roomName)
                .isActive(true)
                .maxParticipants(50000)  // 고정 채팅방은 더 큰 수용량
                .build();

        ChatRoom saved = chatRoomRepository.save(room);
        return ChatRoomResponse.of(saved);
    }


    // 2) 단건 조회
    @Transactional(readOnly = true)
    public ChatRoomResponse getById(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));
        return ChatRoomResponse.of(room);
    }

    // 3) 전체(활성) 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getAllActive() {
        return chatRoomRepository.findByIsActiveTrue().stream()
                .map(ChatRoomResponse::of)
                .toList();
    }

    // 추가: 고정 채팅방만 조회
    @Transactional(readOnly = true)
    public Optional<ChatRoomResponse> getGeneralChatRoom() {
        return chatRoomRepository.findGeneralChatRoom()
                .map(ChatRoomResponse::of);
    }

    // 추가: 경기별 채팅방만 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getGameChatRooms() {
        return chatRoomRepository.findGameChatRooms().stream()
                .map(ChatRoomResponse::of)
                .toList();
    }

    // 4) 수정
    @Transactional
    public ChatRoomResponse update(Long roomId, ChatRoomRequest req) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));
        room.updateRoomName(req.getRoomName());
        room.updateMaxParticipants(req.getMaxParticipants());
        room.updateIsActive(req.getIsActive());
        return ChatRoomResponse.of(room);
    }

    // 5) 삭제
    @Transactional
    public void delete(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new EntityNotFoundException("Room not found: " + roomId);
        }
        chatRoomRepository.deleteById(roomId);
    }

    /**
     * 주어진 날짜(date) 이전에 열린 게임의 채팅방들 중 활성화된 것들을 모두 비활성화합니다.
     *
     * @param date 기준일자 (이 날짜의 00:00 이전 게임만 비활성화 대상)
     */
    @Transactional
    public void disableChatRooms(LocalDate date) {
        // LocalDate → LocalDateTime 변환: date 00:00:00
        LocalDateTime cutoff = date.atStartOfDay();

        // 1) 기준일 이전의 활성 채팅방을 전부 조회
        List<ChatRoom> rooms = chatRoomRepository
                .findByGame_GameDateBeforeAndIsActiveTrue(cutoff);

        // 2) each 로 deactivate() 호출 (dirty checking으로 자동 반영)
        rooms.forEach(c -> c.updateIsActive(false));
    }

    public Long getRoomIdByGameId(Long gameId) {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByGame_GameId(gameId);

        if(chatRoom == null) {
            throw new EntityNotFoundException("Room not found by GameId:" + gameId);
        }

        return chatRoom.getRoomId();
    }
    // 추가: 고정 채팅방의 roomId 조회
    public Long getGeneralChatRoomId() {
        ChatRoom generalRoom = chatRoomRepository.findGeneralChatRoom()
                .orElseThrow(() -> new EntityNotFoundException("General chat room not found"));
        return generalRoom.getRoomId();
    }

    /**
     * 채팅방 상세 조회 (게임 정보 포함)
     */
    @Transactional(readOnly = true)
    public ChatRoomDetailResponse getChatRoomDetail(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        if (room.getGame() == null) {
            // 고정 채팅방인 경우
            return ChatRoomDetailResponse.builder()
                    .roomId(room.getRoomId())
                    .gameId(null)
                    .roomName(room.getRoomName())
                    .isActive(room.getIsActive())
                    .maxParticipants(room.getMaxParticipants())
                    .isGeneralRoom(true)
                    .gameInfo(null)  // 게임 정보 없음
                    .build();
        } else {
            // 경기별 채팅방인 경우
            Game game = room.getGame();

            // Game을 GameDetailInfo로 변환
            GameDetailInfo gameDetailInfo = GameDetailInfo.builder()
                    .gameId(game.getGameId())
                    .homeTeamName(game.getHomeTeam().getTeamName())
                    .homeCode(game.getHomeTeam().getTeamCode())
                    .homeLogoUrl(game.getHomeTeam().getLogoUrl())
                    .awayTeamName(game.getAwayTeam().getTeamName())
                    .awayCode(game.getAwayTeam().getTeamCode())
                    .awayLogoUrl(game.getAwayTeam().getLogoUrl())
                    .gameDateTime(game.getGameDate())
                    .stadium(game.getStadium())
                    .build();

            return ChatRoomDetailResponse.builder()
                    .roomId(room.getRoomId())
                    .gameId(game.getGameId())
                    .roomName(room.getRoomName())
                    .isActive(room.getIsActive())
                    .maxParticipants(room.getMaxParticipants())
                    .isGeneralRoom(false)
                    .gameInfo(gameDetailInfo)
                    .build();
        }
    }

}