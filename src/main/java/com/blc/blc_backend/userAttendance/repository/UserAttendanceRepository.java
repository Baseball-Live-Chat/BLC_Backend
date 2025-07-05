package com.blc.blc_backend.userAttendance.repository;

import com.blc.blc_backend.userAttendance.entity.UserAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserAttendanceRepository extends JpaRepository<UserAttendance, Long> {
    /** 당일(userId, 오늘) 출석 여부 확인 */
    boolean existsByUserIdAndAttendDate(Long userId, LocalDate attendDate);

    /** 특정 기간(userId, startDate~endDate) 출석 기록 조회 */
    List<UserAttendance> findAllByUserIdAndAttendDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}

