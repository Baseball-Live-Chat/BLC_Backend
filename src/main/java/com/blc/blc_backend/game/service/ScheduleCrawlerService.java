package com.blc.blc_backend.game.service;

import com.blc.blc_backend.chatroom.service.ChatRoomService;
import com.blc.blc_backend.game.dto.GameInfo;
import com.blc.blc_backend.game.entity.Game;
import com.blc.blc_backend.game.repository.GameRepository;
import com.blc.blc_backend.team.entity.Team;
import com.blc.blc_backend.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleCrawlerService {

    private static final String GAME_INFO_URL = "https://statiz.sporki.com/schedule/?m=daily&date=";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd";
    private static final String NOT_FOUND_TEAM_ERROR_FORMAT = "%s 팀이 없습니다.";

    private final TeamRepository teamRepo;
    private final GameRepository gameRepo;
    private final ChatRoomService chatRoomService;

    @Transactional
    public void crawlGameInfo(LocalDate date) {
        try {
            Document doc = crawl(date);
            List<GameInfo> gameInfos = getGameInfo(date, doc);
            saveGameInfos(gameInfos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Document crawl(LocalDate date) throws IOException {
        String url = GAME_INFO_URL + date.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        Document doc = Jsoup.connect(url)
                // 1) 브라우저 User-Agent 흉내
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/122.0.0.0 Safari/537.36")
                // 2) 리퍼러(Referer) 헤더 설정
                .referrer("https://statiz.sporki.com/")
                .get();
        return doc;
    }

    private List<GameInfo> getGameInfo(LocalDate date, Document doc) {
        List<GameInfo> gameInfos = new ArrayList<>();

        Elements games = doc.select("div.box_type_boared div.item_box");

        try {
            for (Element gameBox : games) {
                String headText = gameBox.selectFirst("div.sh_box div.box_head").text();
                // 날짜/시간 (예: "정규06-15 17:00 (잠실) 경기전")
                Matcher m = Pattern.compile("(\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2})").matcher(headText);
                if (!m.find()) continue;  // 형식이 다르면 스킵

                String monthDay = m.group(1);
                String timeStr = m.group(2);
                LocalDateTime gameDateTime = LocalDateTime.of(
                        date.withMonth(Integer.parseInt(monthDay.substring(0, 2)))
                                .withDayOfMonth(Integer.parseInt(monthDay.substring(3, 5))),
                        LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                );

                // 구장 정보
                m = Pattern.compile("\\(([^)]+)\\)").matcher(headText);
                String stadium = m.find() ? m.group(1) : "";

                // 팀 정보 파싱 (첫 번째 tr=원정, 두 번째 tr=홈)
                Elements rows = gameBox.select("div.table_type03 table tbody tr");
                if (rows.size() < 2) {
                    continue;
                }

                String awayCode = rows.get(0)
                        .selectFirst("td.align_left")
                        .ownText()
                        .trim();
                String homeCode = rows.get(1)
                        .selectFirst("td.align_left")
                        .ownText()
                        .trim();

                GameInfo gameInfo = GameInfo.builder()
                        .awayCode(awayCode)
                        .homeCode(homeCode)
                        .gameDateTime(gameDateTime)
                        .stadium(stadium)
                        .build();

                log.info("[crawlGameInfo] {}]", gameInfo);
                gameInfos.add(gameInfo);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            gameInfos = new ArrayList<>();
        }
        return gameInfos;
    }

    private void saveGameInfos(List<GameInfo> gameInfos) {
        for (GameInfo info : gameInfos) {
            Game saved = saveGame(info);
            createChatRoomFor(saved);
        }
    }

    private Game saveGame(GameInfo gameInfo) {
        String awayCode = gameInfo.getAwayCode();
        String homeCode = gameInfo.getHomeCode();
        LocalDateTime gameDateTime = gameInfo.getGameDateTime();
        String stadium = gameInfo.getStadium();

        Team away = findTeam(awayCode);
        Team home = findTeam(homeCode);

        if (gameRepo.existsByHomeTeamAndAwayTeamAndGameDate(home, away, gameDateTime)) {
            return null;
        }

        Game game = Game.builder()
                .homeTeam(home)
                .awayTeam(away)
                .gameDate(gameDateTime)
                .stadium(stadium)
                .build();

        return gameRepo.save(game);
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