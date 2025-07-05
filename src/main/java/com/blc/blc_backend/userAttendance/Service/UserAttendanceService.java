package com.blc.blc_backend.userAttendance.Service;

import com.blc.blc_backend.user.service.UserService;
import com.blc.blc_backend.userAttendance.dto.UserAttendanceRequest;
import com.blc.blc_backend.userAttendance.dto.UserAttendanceResponse;
import com.blc.blc_backend.userAttendance.entity.UserAttendance;
import com.blc.blc_backend.userAttendance.repository.UserAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAttendanceService {
    private final UserAttendanceRepository attendanceRepo;
    private final UserService userService;      // 포인트 적립 담당

    /** 하루 출석 시 지급할 포인트 */
    private static final long DAILY_POINT = 1000;

    /**
     * userId 사용자의 당일 출석을 기록하고,
     * 최초 출석 시 포인트를 적립합니다.
     */
    public void recordAttendance(Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 이미 출석했으면 아무 동작 없이 리턴
        if (attendanceRepo.existsByUserIdAndAttendDate(userId, today)) {
            return;
        }

        // 1) 출석 기록 저장
        UserAttendance attendance = UserAttendance.builder()
                .userId(userId)
                .attendDate(today)
                .build();

        attendanceRepo.save(attendance);

        userService.addUserPoints(userId, DAILY_POINT);
    }

    /**
     * 요청된 year, month, userId 에 해당하는 일별 출석 현황을 반환
     */
    public UserAttendanceResponse getUserAttendance(UserAttendanceRequest request) {
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        // 1) 해당 기간 중 출석된 엔티티만 조회
        List<UserAttendance> attendanceList = attendanceRepo
                .findAllByUserIdAndAttendDateBetween(request.getUserId(), start, end);

        // 2) LocalDate 리스트로 변환
        List<LocalDate> attendDates = attendanceList.stream()
                .map(UserAttendance::getAttendDate)
                .sorted()  // Optional: 오름차순 정렬
                .collect(Collectors.toList());

        // 3) Response 조립
        UserAttendanceResponse resp = new UserAttendanceResponse();
        resp.setUserId(request.getUserId());
        resp.setAttendDates(attendDates);
        return resp;
    }
}

