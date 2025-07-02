package com.blc.blc_backend.game.service;

import com.blc.blc_backend.chatroom.service.ChatRoomService;
import com.blc.blc_backend.game.dto.GameInfo;
import com.blc.blc_backend.game.entity.Game;
import com.blc.blc_backend.game.repository.GameRepository;
import com.blc.blc_backend.team.entity.Team;
import com.blc.blc_backend.team.repository.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleCrawlerService {

    // KBO Schedule API 엔드포인트
    private static final String API_URL = "https://www.koreabaseball.com/ws/Schedule.asmx/GetScheduleList";

    private final TeamRepository teamRepo;
    private final GameRepository gameRepo;
    private final ChatRoomService chatRoomService;

    /**
     * 지정된 날짜(date)의 경기 정보를 가져와 저장합니다.
     */
    @Transactional
    public void crawlGameInfo(LocalDate date) {
        try {
            List<GameInfo> gameInfos = fetchGamesByDate(date);
            saveGameInfos(gameInfos);
        } catch (Exception e) {
            log.error("일정 파싱 실패 (date={}):", date, e);
        }
    }

    /**
     * KBO API를 호출하여 주어진 날짜의 경기만 필터링해 파싱합니다.
     */
    private List<GameInfo> fetchGamesByDate(LocalDate date) throws Exception {
        // 1) 폼 데이터 생성
        String year  = date.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = date.format(DateTimeFormatter.ofPattern("MM"));
        // teamId 빈 문자열로 하면 전체 팀
        String form = "leId=1"
                + "&srIdList=0,9,6"   // 0,9,6 = 정규시즌
                + "&seasonId=" + year
                + "&gameMonth=" + month
                + "&teamId=";

        // 2) HTTP 요청 준비
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        // 3) 동기 요청 및 응답 수신
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        String json = resp.body();

        List<GameInfo> gameInfos = parseTodayGamesFromJson(json);

        System.out.println(gameInfos);
        return gameInfos;
    }

    /**
     * 파싱된 GameInfo 리스트를 DB에 저장하고, 신규 경기마다 채팅방을 생성합니다.
     */
    private void saveGameInfos(List<GameInfo> gameInfos) {
        for (GameInfo info : gameInfos) {
            Game saved = saveGame(info);
            createChatRoomFor(saved);
        }
    }

    private Game saveGame(GameInfo gameInfo) {
        Team away = findTeam(gameInfo.getAwayCode());
        Team home = findTeam(gameInfo.getHomeCode());
        LocalDateTime dt = gameInfo.getGameDateTime();

        // 이미 저장된 경기인지 체크
        if (gameRepo.existsByHomeTeamAndAwayTeamAndGameDate(home, away, dt)) {
            return null;
        }

        Game game = Game.builder()
                .homeTeam(home)
                .awayTeam(away)
                .gameDate(dt)
                .stadium(gameInfo.getStadium())
                .build();

        return gameRepo.save(game);
    }

    public List<GameInfo> parseTodayGamesFromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode rows = root.path("rows");

        // 오늘 "MM.dd"
        String todayStr = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("MM.dd"));
        String currentDay = null;

        List<GameInfo> gameInfos = new ArrayList<>();

        for (JsonNode rowNode : rows) {
            JsonNode cells = rowNode.path("row");
            if (cells.isMissingNode() || cells.size() == 0) continue;

            // 첫 번째 셀을 보고 'day'인지 판단
            JsonNode first = cells.get(0);
            boolean hasDay = "day".equals(first.path("Class").asText());

            // 날짜 갱신 (hasDay == true 인 경우에만)
            if (hasDay) {
                String dateText = Jsoup.parse(first.path("Text").asText()).text();  // ex "07.02(수)"
                currentDay = dateText.substring(0, 5);                            // "07.02"
            }
            // 오늘 날짜가 아니면 스킵
            if (!todayStr.equals(currentDay)) continue;

            // 컬럼 인덱스 결정
            int timeIdx    = hasDay ? 1 : 0;
            int playIdx    = hasDay ? 2 : 1;
            int stadiumIdx = hasDay ? 7 : 6;

            // 시간 파싱
            String timeHtml = cells.get(timeIdx).path("Text").asText();        // ex "<b>18:30</b>"
            String timeStr  = Jsoup.parse(timeHtml).text();                    // "18:30"
            LocalTime time  = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));

            // 팀 파싱
            String playHtml = cells.get(playIdx).path("Text").asText();
            String playText = Jsoup.parse(playHtml).text();                    // ex "삼성 4 vs 1 두산" or "LG vs 롯데"
            String[] parts  = playText.split("vs");
            if (parts.length < 2) continue;
            String awayName = parts[0].replaceAll("\\d", "").trim();
            String homeName = parts[1].replaceAll("\\d", "").trim();

            // 구장
            String stadiumHtml = cells.get(stadiumIdx).path("Text").asText();
            String stadium     = Jsoup.parse(stadiumHtml).text();

            // 오늘 날짜 + 시간 → LocalDateTime
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            LocalDateTime gameDateTime = LocalDateTime.of(today, time);

            gameInfos.add(GameInfo.builder()
                    .awayCode(awayName)
                    .homeCode(homeName)
                    .gameDateTime(gameDateTime)
                    .stadium(stadium)
                    .build());
        }

        return gameInfos;
    }

    private Team findTeam(String code) {
        return teamRepo.findByTeamCode(code)
                .orElseThrow(() -> new IllegalStateException(code + " 팀이 없습니다."));
    }

    private void createChatRoomFor(Game game) {
        if (game == null) return;
        String roomName = String.format("%s : %s vs %s",
                game.getGameDate(), game.getAwayTeam().getTeamName(), game.getHomeTeam().getTeamName());
        chatRoomService.create(game.getGameId(), roomName);
    }
}