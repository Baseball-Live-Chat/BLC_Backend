package com.blc.blc_backend.userAttendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.*;

@Entity
@Table(
        name = "user_attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_attend",
                columnNames = {"user_id", "attend_date"}
        )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate attendDate;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
}
