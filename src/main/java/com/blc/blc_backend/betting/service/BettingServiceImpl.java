package com.blc.blc_backend.betting.service;

import com.blc.blc_backend.betting.dto.BettingStatsProjection;
import com.blc.blc_backend.betting.dto.BettingStatsResponseDto;
import com.blc.blc_backend.betting.dto.UserBetStatusDto;
import com.blc.blc_backend.betting.entity.GameBet;
import com.blc.blc_backend.betting.repository.GameBetRepository;
import com.blc.blc_backend.game.entity.Game;
import com.blc.blc_backend.game.repository.GameRepository;
import com.blc.blc_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BettingServiceImpl implements BettingService {

    private final GameBetRepository gameBetRepository;
    private final GameRepository gameRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int MAX_BET_POINTS = 10000; // 최대 총 베팅 금액
    private static final int MIN_SINGLE_BET = 10;    // 한 번에 최소 베팅

    @Override
    public void placeBet(Long userId, Long gameId, Long predictedWinnerTeamId, int betPoints) {
        log.info("베팅 요청 - userId: {}, gameId: {}, teamId: {}, points: {}",
                userId, gameId, predictedWinnerTeamId, betPoints);

        validateBet(userId, gameId, predictedWinnerTeamId, betPoints);

        GameBet bet = GameBet.builder()
                .gameId(gameId)
                .userId(userId)
                .predictedWinnerTeamId(predictedWinnerTeamId)
                .betPoints(betPoints)
                .build();

        gameBetRepository.save(bet);
        subtractUserPoints(userId, betPoints);
        broadcastBettingUpdate(gameId);

        log.info("베팅 완료 - betId: {}, 누적: {}포인트",
                bet.getBetId(), getTotalUserBetPoints(userId, gameId));
    }

    @Override
    @Transactional(readOnly = true)
    public BettingStatsResponseDto calculateBettingStats(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("경기를 찾을 수 없습니다: " + gameId));

        BettingStatsProjection stats = gameBetRepository.getBettingStatsByGameId(
                gameId,
                game.getHomeTeam().getTeamId(),
                game.getAwayTeam().getTeamId()
        );

        long homePoints = stats.getHomePoints() != null ? stats.getHomePoints() : 0;
        long awayPoints = stats.getAwayPoints() != null ? stats.getAwayPoints() : 0;
        int homeCount = stats.getHomeCount() != null ? stats.getHomeCount() : 0;
        int awayCount = stats.getAwayCount() != null ? stats.getAwayCount() : 0;
        long totalPoints = stats.getTotalPoints() != null ? stats.getTotalPoints() : 0;
        int totalCount = stats.getTotalCount() != null ? stats.getTotalCount() : 0;

        double homeOdds = homePoints > 0 ? (double) totalPoints / homePoints : 999.0;
        double awayOdds = awayPoints > 0 ? (double) totalPoints / awayPoints : 999.0;

        return BettingStatsResponseDto.builder()
                .gameId(gameId)
                .homeTeamName(game.getHomeTeam().getTeamName())
                .awayTeamName(game.getAwayTeam().getTeamName())
                .homeTeamBetPoints(homePoints)
                .awayTeamBetPoints(awayPoints)
                .homeTeamBetCount(homeCount)
                .awayTeamBetCount(awayCount)
                .homeTeamOdds(Math.round(homeOdds * 100.0) / 100.0)
                .awayTeamOdds(Math.round(awayOdds * 100.0) / 100.0)
                .totalBetPoints(totalPoints)
                .totalBetCount(totalCount)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserBetStatusDto getUserBetStatus(Long userId, Long gameId) {
        List<GameBet> userBets = gameBetRepository.findByUserIdAndGameIdAndSettledAtIsNull(userId, gameId);

        int totalBetPoints = userBets.stream().mapToInt(GameBet::getBetPoints).sum();
        int remainingPoints = MAX_BET_POINTS - totalBetPoints;

        Long predictedTeamId = userBets.isEmpty() ? null : userBets.get(0).getPredictedWinnerTeamId();

        return UserBetStatusDto.builder()
                .gameId(gameId)
                .totalBetPoints(totalBetPoints)
                .remainingPoints(Math.max(0, remainingPoints))
                .predictedWinnerTeamId(predictedTeamId)
                .betCount(userBets.size())
                .canBet(remainingPoints >= MIN_SINGLE_BET)
                .build();
    }

    @Override
    public void settleBets(Long gameId, Long winnerTeamId) {
        log.info("베팅 정산 시작 - gameId: {}, winnerTeamId: {}", gameId, winnerTeamId);

        List<GameBet> unsettledBets = gameBetRepository.findByGameIdAndSettledAtIsNull(gameId);

        if (unsettledBets.isEmpty()) {
            throw new IllegalStateException("이미 정산된 경기입니다");
        }

        if (winnerTeamId == null) {
            handleDrawOrCancelledGame(unsettledBets);
            return;
        }

        handleWinLoseGame(unsettledBets, winnerTeamId);
        log.info("베팅 정산 완료 - gameId: {}", gameId);
    }

    private void validateBet(Long userId, Long gameId, Long predictedWinnerTeamId, int betPoints) {
        // 1. 현재 누적 베팅 금액 조회
        Integer currentTotalBet = gameBetRepository.getTotalBetPointsByUserAndGame(userId, gameId);
        int totalAfterBet = currentTotalBet + betPoints;

        log.debug("베팅 검증 - 현재 누적: {}P, 추가 베팅: {}P, 총합: {}P",
                currentTotalBet, betPoints, totalAfterBet);

        // 2. 한 번에 베팅하는 최소 금액 확인
        if (betPoints < MIN_SINGLE_BET) {
            throw new IllegalArgumentException(
                    String.format("최소 %d포인트 이상 베팅해야 합니다", MIN_SINGLE_BET));
        }

        // 3. 누적 베팅 상한선 확인
        if (totalAfterBet > MAX_BET_POINTS) {
            int remainingPoints = MAX_BET_POINTS - currentTotalBet;
            throw new IllegalArgumentException(
                    String.format("베팅 한도 초과! 현재 누적: %dP, 추가 가능: %dP (최대: %dP)",
                            currentTotalBet, remainingPoints, MAX_BET_POINTS));
        }

        // 4. 기존 베팅과 다른 팀에 베팅하는지 확인
        validateTeamConsistency(userId, gameId, predictedWinnerTeamId);

        // 5. 경기 존재 및 베팅 가능 시간 확인
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("경기를 찾을 수 없습니다"));

        if (game.getGameDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("베팅 시간이 마감되었습니다");
        }

        // 6. 팀 유효성 확인
        if (!predictedWinnerTeamId.equals(game.getHomeTeam().getTeamId()) &&
                !predictedWinnerTeamId.equals(game.getAwayTeam().getTeamId())) {
            throw new IllegalArgumentException("해당 경기에 참여하지 않는 팀입니다");
        }

        // 7. 사용자 포인트 확인
        if (!userService.hasEnoughPoints(userId, betPoints)) {
            Long currentPoints = userService.getUserPoints(userId);
            throw new IllegalStateException(
                    String.format("포인트가 부족합니다. 보유: %dP, 필요: %dP", currentPoints, betPoints));
        }
    }

    private void validateTeamConsistency(Long userId, Long gameId, Long predictedWinnerTeamId) {
        List<GameBet> existingBets = gameBetRepository.findByUserIdAndGameIdAndSettledAtIsNull(userId, gameId);

        if (!existingBets.isEmpty()) {
            Long firstBetTeamId = existingBets.get(0).getPredictedWinnerTeamId();
            if (!firstBetTeamId.equals(predictedWinnerTeamId)) {
                throw new IllegalArgumentException("이미 다른 팀에 베팅하셨습니다. 같은 팀에만 추가 베팅 가능합니다.");
            }
        }
    }

    private void subtractUserPoints(Long userId, int points) {
        userService.subtractUserPoints(userId, points);
    }

    private void addUserPoints(Long userId, int points) {
        userService.addUserPoints(userId, points);
    }

    private void broadcastBettingUpdate(Long gameId) {
        try {
            BettingStatsResponseDto stats = calculateBettingStats(gameId);
            messagingTemplate.convertAndSend("/topic/betting/" + gameId, stats);
        } catch (Exception e) {
            log.error("베팅 현황 브로드캐스트 실패 - gameId: {}", gameId, e);
        }
    }

    private void handleDrawOrCancelledGame(List<GameBet> bets) {
        LocalDateTime now = LocalDateTime.now();
        for (GameBet bet : bets) {
            addUserPoints(bet.getUserId(), bet.getBetPoints());
            bet.setSettledAt(now);
        }
        gameBetRepository.saveAll(bets);
    }

    private void handleWinLoseGame(List<GameBet> bets, Long winnerTeamId) {
        List<GameBet> winningBets = bets.stream()
                .filter(bet -> bet.getPredictedWinnerTeamId().equals(winnerTeamId))
                .toList();

        if (winningBets.isEmpty()) {
            markBetsAsSettled(bets);
            return;
        }

        int totalBetPoints = bets.stream().mapToInt(GameBet::getBetPoints).sum();
        int winningBetPoints = winningBets.stream().mapToInt(GameBet::getBetPoints).sum();

        for (GameBet winningBet : winningBets) {
            double ratio = (double) winningBet.getBetPoints() / winningBetPoints;
            int reward = (int) (totalBetPoints * ratio);
            addUserPoints(winningBet.getUserId(), reward);
        }

        markBetsAsSettled(bets);
    }

    private void markBetsAsSettled(List<GameBet> bets) {
        LocalDateTime now = LocalDateTime.now();
        bets.forEach(bet -> bet.setSettledAt(now));
        gameBetRepository.saveAll(bets);
    }

    private int getTotalUserBetPoints(Long userId, Long gameId) {
        return gameBetRepository.getTotalBetPointsByUserAndGame(userId, gameId);
    }
}