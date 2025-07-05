// ScheduleCrawlerScheduler.java
package com.blc.blc_backend.game.scheduler;

import com.blc.blc_backend.chatroom.service.ChatRoomService;
import com.blc.blc_backend.game.service.ScheduleCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class ScheduleCrawlerScheduler {

    @Value("${crawler.schedule.zone}")
    private String scheduleZone;

    private final ScheduleCrawlerService crawlerService;
    private final ChatRoomService chatRoomService;

    @Scheduled(cron = "${crawler.schedule.cron}", zone = "${crawler.schedule.zone}")
    public void runDailyCrawl() {
        ZoneId zone = ZoneId.of(scheduleZone);
        LocalDate today = LocalDate.now(zone);

        crawlerService.crawlGameInfo(today);
        chatRoomService.disableChatRooms(today);
    }
}
