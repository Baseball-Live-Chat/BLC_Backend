package com.blc.blc_backend.game.service;

import com.blc.blc_backend.game.entity.Game;
import com.blc.blc_backend.game.repository.GameRepository;
import com.blc.blc_backend.team.entity.Team;
import com.blc.blc_backend.team.repository.TeamRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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
        // Mockito 어노테이션 초기화
        MockitoAnnotations.openMocks(this);
        // 실제 서비스 인스턴스에 스파이를 적용해 crawl() 제어 가능
        service = Mockito.spy(new ScheduleCrawlerService(teamRepo, gameRepo));
    }

    @Test
    void crawlGameInfo_savesNewGame() throws IOException {
        LocalDate date = LocalDate.of(2025, 6, 15);

        // 1) crawl(date) 호출 시 SAMPLE_HTML을 파싱한 Document를 반환하도록 설정
        Document doc = Jsoup.parse(SAMPLE_HTML);
        doReturn(doc).when(service).crawl(date);

        // 2) 팀 리포지토리 반환값 설정
        Team away = Team.builder().teamCode("롯데").build();
        Team home = Team.builder().teamCode("SSG").build();
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
    }

    @Test
    void crawlGameInfo_skipsExistingGame() throws IOException {
        LocalDate date = LocalDate.of(2025, 6, 15);

        // crawl(date) 호출 시 동일한 Document 반환
        Document doc = Jsoup.parse(SAMPLE_HTML);
        doReturn(doc).when(service).crawl(date);

        // 팀 조회 스텁 설정
        Team away = Team.builder().teamCode("롯데").build();
        Team home = Team.builder().teamCode("SSG").build();
        when(teamRepo.findByTeamCode("롯데")).thenReturn(Optional.of(away));
        when(teamRepo.findByTeamCode("SSG")).thenReturn(Optional.of(home));

        // 이미 해당 경기가 존재하는 경우 시뮬레이션
        LocalDateTime expectedDateTime = LocalDateTime.of(date, LocalTime.of(17, 0));
        when(gameRepo.existsByHomeTeamAndAwayTeamAndGameDate(home, away, expectedDateTime))
                .thenReturn(true);

        // 실행
        service.crawlGameInfo(date);

        // save()가 호출되지 않음을 검증
        verify(gameRepo, never()).save(any(Game.class));
    }
}
