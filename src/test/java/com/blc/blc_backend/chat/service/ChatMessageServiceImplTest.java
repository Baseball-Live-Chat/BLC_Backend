package com.blc.blc_backend.chat.service;
import static org.junit.jupiter.api.Assertions.*;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.entity.ChatMessage;
import com.blc.blc_backend.chat.repository.ChatMessageRepository;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {

    @Mock
    private ChatMessageRepository repository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatMessageServiceImpl service;

    private static final Long ROOM_ID = 10L;
    private static final Long TEAM_ID = 20L;

    @BeforeEach
    void setUp() {
        // 공통적으로 필요한 초기 설정이 있으면 여기서
    }

    @Test
    @DisplayName("익명 메시지 생성 시 userService 호출 없이 nickname='익명'을 반환한다")
    void createMessage_anonymous() {
        // given
        ChatMessageRequestDto dto = mock(ChatMessageRequestDto.class);
        // dto에 setter가 없으니 생성자나 리플렉션으로 주입하거나, Lombok @Builder를 쓰셨다면 그걸 사용하세요.
        // 여기서는 편의를 위해 Mockito로 모킹
        given(dto.getUserId()).willReturn(null);
        given(dto.getTeamId()).willReturn(TEAM_ID);
        given(dto.getContent()).willReturn("hello");
        given(dto.getType()).willReturn(null);

        ChatMessage saved = new ChatMessage();
        saved.setRoomId(ROOM_ID);
        saved.setUserId(-1L);
        saved.setTeamId(TEAM_ID);
        saved.setMessageContent("hello");
        saved.setMessageType(null);
        saved.setCreatedAt(LocalDateTime.now());
        given(repository.save(any(ChatMessage.class))).willReturn(saved);

        // when
        ChatMessageResponseDto result = service.createMessage(ROOM_ID, dto);

        // then
        assertThat(result.getUserId()).isEqualTo(-1L);
        assertThat(result.getNickname()).isEqualTo("익명");
        // userService.getUserById()가 절대 호출되지 않아야 함
        then(userService).should(never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("회원 메시지 생성 시 userService에서 nickname을 조회한다")
    void createMessage_member() {
        // given
        ChatMessageRequestDto dto = mock(ChatMessageRequestDto.class);
        given(dto.getUserId()).willReturn(5L);
        given(dto.getTeamId()).willReturn(TEAM_ID);
        given(dto.getContent()).willReturn("hi");
        given(dto.getType()).willReturn(null);

        ChatMessage saved = new ChatMessage();
        saved.setRoomId(ROOM_ID);
        saved.setUserId(5L);
        saved.setTeamId(TEAM_ID);
        saved.setMessageContent("hi");
        saved.setMessageType(null);
        saved.setCreatedAt(LocalDateTime.now());
        given(repository.save(any(ChatMessage.class))).willReturn(saved);

        UserResponseDto userDto = UserResponseDto.builder().nickname("kim").build();
        given(userService.getUserById(5L)).willReturn(userDto);

        // when
        ChatMessageResponseDto result = service.createMessage(ROOM_ID, dto);

        // then
        assertThat(result.getUserId()).isEqualTo(5L);
        assertThat(result.getNickname()).isEqualTo("kim");
        then(userService).should(times(1)).getUserById(5L);
    }

    @Test
    @DisplayName("getMessagesByRoom은 저장된 메시지를 순서대로 리턴하고, nickname 조회도 수행한다")
    void getMessagesByRoom_mixed() {
        // given
        ChatMessage anon = new ChatMessage();
        anon.setRoomId(ROOM_ID);
        anon.setUserId(-1L);
        anon.setTeamId(TEAM_ID);
        anon.setMessageContent("a");
        anon.setMessageType(null);
        anon.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        ChatMessage userMsg = new ChatMessage();
        userMsg.setRoomId(ROOM_ID);
        userMsg.setUserId(7L);
        userMsg.setTeamId(TEAM_ID);
        userMsg.setMessageContent("b");
        userMsg.setMessageType(null);
        userMsg.setCreatedAt(LocalDateTime.now());

        given(repository.findByRoomIdOrderByCreatedAtAsc(ROOM_ID))
                .willReturn(List.of(anon, userMsg));

        // 익명도 userService 호출 없이 "익명" 처리하려면, 아래처럼 모킹 필요
        // 현재 impl은 익명에 대해 userService 호출하므로, 테스트 통과를 위해 스텁을 하나 제공
        UserResponseDto stubAnonUser = UserResponseDto.builder().nickname("익명").build();

        UserResponseDto stubUser = UserResponseDto.builder().nickname("park").build();
        given(userService.getUserById(7L)).willReturn(stubUser);

        // when
        var list = service.getMessagesByRoom(ROOM_ID);

        // then
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getNickname()).isEqualTo("익명");
        assertThat(list.get(1).getNickname()).isEqualTo("park");

        then(userService).should(never()).getUserById(-1L);
        then(userService).should(times(1)).getUserById(7L);
    }
}
