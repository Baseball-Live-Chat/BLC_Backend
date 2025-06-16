package com.blc.blc_backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    private String teamName;

    @Column(unique = true)
    private String teamCode;

    private String logoUrl;
    private String teamColor;
    private String league;
}