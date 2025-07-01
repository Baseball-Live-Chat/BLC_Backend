// ScheduleCrawlerScheduler.java
package com.blc.blc_backend.game.scheduler;

import com.blc.blc_backend.chatroom.service.ChatRoomService;
import com.blc.blc_backend.game.service.ScheduleCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ScheduleCrawlerScheduler {

    private final ScheduleCrawlerService crawlerService;
    private final ChatRoomService chatRoomService;

    @Scheduled(cron = "${crawler.schedule.cron}", zone = "${crawler.schedule.zone}")
    public void runDailyCrawl() {
        crawlerService.crawlGameInfo(LocalDate.now());
        chatRoomService.disableChatRooms(LocalDate.now());
    }
}
