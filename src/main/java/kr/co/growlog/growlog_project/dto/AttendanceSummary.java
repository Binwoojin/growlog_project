package kr.co.growlog.growlog_project.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceSummary {

    // 현재 연속 출석 일수
    private int currentStreak;

    // 오늘 출석 완료 여부
    private boolean attendedToday;
}
