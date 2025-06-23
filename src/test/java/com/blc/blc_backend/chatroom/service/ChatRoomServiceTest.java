package com.blc.blc_backend.chatroom.service;

import com.blc.blc_backend.chatroom.entity.ChatRoom;
import com.blc.blc_backend.chatroom.repository.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @ExtendWith(MockitoExtension.class)


    @Test
    @DisplayName("게임 ID로 조회 시, 채팅방이 존재하면 roomId를 반환한다")
    void findRoomIdByGameId_ReturnsRoomId_WhenChatRoomExists() {
        // given
        Long gameId = 1L;
        // ChatRoom은 builder로만 생성하고, ID는 reflection으로 주입
        ChatRoom mockRoom = ChatRoom.builder()
                .game(null)              // game은 실제 값이 필요 없으면 null
                .roomName("test-room")
                .isActive(true)
                .maxParticipants(100)
                .build();
        ReflectionTestUtils.setField(mockRoom, "roomId", 123L);

        // gameId 로 조회시, mockRoom이 반환되도록 설계
        when(chatRoomRepository.findChatRoomByGame_GameId(gameId))
                .thenReturn(mockRoom);

        // when
        Long result = chatRoomService.getRoomIdByGameId(gameId);

        // then
        assertThat(result).isEqualTo(123L);
        // ArgumentCaptor로 인자 캡처 및 AssertJ로 검증
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        then(chatRoomRepository).should().findChatRoomByGame_GameId(captor.capture());
        assertThat(captor.getValue()).isEqualTo(gameId);
    }

    @Test
    @DisplayName("게임 ID로 조회 시, 채팅방이 없으면 EntityNotFoundException을 던지고 호출 인자를 검증한다")
    void findRoomIdByGameId_ThrowsAndAssertsInvocationArg() {
        // given
        Long gameId = 2L;
        given(chatRoomRepository.findChatRoomByGame_GameId(gameId))
                .willReturn(null);

        // when & then
        assertThatThrownBy(() -> chatRoomService.getRoomIdByGameId(gameId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Room not found by GameId:" + gameId);

        // 호출 인자 캡처 및 검증
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        then(chatRoomRepository).should().findChatRoomByGame_GameId(captor.capture());
        assertThat(captor.getValue()).isEqualTo(gameId);
    }
}