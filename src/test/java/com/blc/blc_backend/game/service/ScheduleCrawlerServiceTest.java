package com.blc.blc_backend.game.service;

import com.blc.blc_backend.chatroom.service.ChatRoomService;
import com.blc.blc_backend.game.entity.Game;
import com.blc.blc_backend.game.repository.GameRepository;
import com.blc.blc_backend.team.entity.Team;
import com.blc.blc_backend.team.repository.TeamRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleCrawlerServiceTest {

    @Mock
    private TeamRepository teamRepo;

    @Mock
    private GameRepository gameRepo;

    @Mock
    private ChatRoomService chatRoomService;

    // crawl(...) 메서드를 오버라이드하기 위해 서비스에 스파이 적용
    private ScheduleCrawlerService service;

    // 최소 HTML 스니펫(경기 블록 1개)
    private static final String SAMPLE_HTML = """
        <div class="box_type_boared">
          <div class="item_box">
            <div class="sh_box">
              <div class="box_head">정규06-15 17:00 (잠실) 경기전</div>
            </div>
            <div class="table_type03">
              <table>
                <tbody>
                  <tr>
                    <td class="align_left"><img src=""/> 롯데</td>
                  </tr>
                  <tr>
                    <td class="align_left"><img src=""/> SSG</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        """;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = Mockito.spy(new ScheduleCrawlerService(teamRepo, gameRepo, chatRoomService));

        // 저장된 Game 객체에 임의의 ID를 부여하고, 그 객체를 반환하도록 설정
        when(gameRepo.save(any(Game.class))).thenAnswer(invocation -> {
            Game g = invocation.getArgument(0);
            // private field인 gameId에 리플렉션으로 값 세팅
            ReflectionTestUtils.setField(g, "gameId", 1L);
            return g;
        });
    }

    @Test
    void crawlGameInfo_savesNewGame_and_createsChatRoom() throws IOException {
        LocalDate date = LocalDate.of(2025, 6, 15);

        // 1) crawl(date) 호출 시 SAMPLE_HTML을 파싱한 Document를 반환하도록 설정
        Document doc = Jsoup.parse(SAMPLE_HTML);
        doReturn(doc).when(service).crawl(date);

        // 2) 팀 리포지토리 반환값 설정
        Team away = Team.builder().teamCode("롯데").teamName("롯데").build();
        Team home = Team.builder().teamCode("SSG").teamName("SSG").build();
        when(teamRepo.findByTeamCode("롯데")).thenReturn(Optional.of(away));
        when(teamRepo.findByTeamCode("SSG")).thenReturn(Optional.of(home));

        // 3) 해당 경기가 아직 존재하지 않는 경우 시뮬레이션
        LocalDateTime expectedDateTime = LocalDateTime.of(date, LocalTime.of(17, 0));
        when(gameRepo.existsByHomeTeamAndAwayTeamAndGameDate(home, away, expectedDateTime))
                .thenReturn(false);

        // 4) 테스트 실행
        service.crawlGameInfo(date);

        // 5) gameRepo.save(...)가 한 번만 호출되었는지 검증하고, 저장된 Game 객체 필드 검사
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepo, times(1)).save(captor.capture());

        Game saved = captor.getValue();
        assertEquals(home, saved.getHomeTeam(), "홈팀이 정확히 매핑되어야 합니다.");
        assertEquals(away, saved.getAwayTeam(), "어웨이팀이 정확히 매핑되어야 합니다.");
        assertEquals(expectedDateTime, saved.getGameDate(), "경기 시간이 정확히 매핑되어야 합니다.");
        assertEquals("잠실", saved.getStadium(), "경기장이 정확히 매핑되어야 합니다.");

        // 6) chatRoomService.create(...)가 한 번 호출되었는지, 올바른 파라미터와 함께 호출되었는지 검증
        String expectedRoomName = String.format("%s : %s vs %s",
                expectedDateTime, away.getTeamName(), home.getTeamName());
        verify(chatRoomService, times(1)).create(1L, expectedRoomName);
    }

    @Test
    void crawlGameInfo_skipsExistingGame() throws IOException {
        LocalDate date = LocalDate.of(2025, 6, 15);

        // crawl(date) 호출 시 동일한 Document 반환
        Document doc = Jsoup.parse(SAMPLE_HTML);
        doReturn(doc).when(service).crawl(date);

        // 팀 조회 스텁 설정
        Team away = Team.builder().teamCode("롯데").teamName("롯데").build();
        Team home = Team.builder().teamCode("SSG").teamName("SSG").build();
        when(teamRepo.findByTeamCode("롯데")).thenReturn(Optional.of(away));
        when(teamRepo.findByTeamCode("SSG")).thenReturn(Optional.of(home));

        // 이미 해당 경기가 존재하는 경우 시뮬레이션
        LocalDateTime expectedDateTime = LocalDateTime.of(date, LocalTime.of(17, 0));
        when(gameRepo.existsByHomeTeamAndAwayTeamAndGameDate(home, away, expectedDateTime))
                .thenReturn(true);

        // 실행
        service.crawlGameInfo(date);

        // save() 호출 없이 건너뛰어야 함
        verify(gameRepo, never()).save(any(Game.class));
        // chatRoomService.create()도 호출되지 않아야 함
        verify(chatRoomService, never()).create(any(), anyString());
    }
}
