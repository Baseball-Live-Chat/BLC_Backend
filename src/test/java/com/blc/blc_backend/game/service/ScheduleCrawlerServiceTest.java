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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ScheduleCrawlerServiceTest {
    @Autowired
    ScheduleCrawlerService crawlerService;
    @Autowired
    ChatRoomService chatRoomService2;

    @Test
    @Transactional
    void makeTest() {
        crawlerService.crawlGameInfo(LocalDate.of(2025,6,24));
    }

    @Test
    @Transactional
    void disableTest2() {
        chatRoomService2.disableChatRooms(LocalDate.of(2025,6,25));
    }
}
