package com.blc.blc_backend.userAttendance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserAttendanceResponse {
    private String nickname;
    /** 출석한 날짜 리스트(yyyy-MM-dd 형식) */
    private List<LocalDate> attendDates;
}
