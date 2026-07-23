package kr.co.growlog.growlog_project.service;

import kr.co.growlog.growlog_project.dto.AttendanceSummary;
import kr.co.growlog.growlog_project.entity.Goal;
import kr.co.growlog.growlog_project.entity.GrowthRecord;
import kr.co.growlog.growlog_project.repository.GoalRepository;
import kr.co.growlog.growlog_project.repository.GrowthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final GoalRepository goalRepository;
    private final GrowthRecordRepository growthRecordRepository;

    // 로그인한 회원의 현재 출석 상태를 계산

    // 출석 인정 조건
    // 1. 해당 날짜에 목표를 1개 이상 등록했거나
    // 2. 해당 날짜에 성장 기록을 1개 이상 등록한 경우
    public AttendanceSummary getAttendanceSummary(Long memberNo) {

        // 로그인한 회원이 등록한 목표 전체를 최신순으로 조회
        List<Goal> goals = goalRepository.findByMemberMemberNoOrderByCreatedAtDesc(memberNo);

        // 로그인한 회원이 등록한 성장 기록 전체를 최신순으로 조회
        List<GrowthRecord> records = growthRecordRepository.findByMemberMemberNoOrderByCreatedAtDesc(memberNo);

        // 목표 또는 성장 기록을 등록한 날짜를 출석일로 저장

        // Set은 중복 값을 허용하지 않기 때문에
        // 같은 날 목표와 기록을 모두 등록했더라도 하루 출석으로 계산
        Set<LocalDate> attendanceDates = new HashSet<>();

        // 목표 등록일을 출석 날짜에 추가
        for (Goal goal : goals) {
            LocalDate goalDate = goal.getCreatedAt().toLocalDate();
            attendanceDates.add(goalDate);
        }

        // 성장 기록 등록일을 출석 날짜에 추가
        for (GrowthRecord record : records) {
            LocalDate recordDate = record.getCreatedAt().toLocalDate();
            attendanceDates.add(recordDate);
        }

        LocalDate today = LocalDate.now();

        // 오늘 목표 또는 성장 기록을 등록했는지 확인
        boolean attendToday = attendanceDates.contains(today);

        // 오늘 축석을 완료한 경우 오늘부터 계산
        // 오늘 아직 출석하지 않은 경우에는 하루가 끝나지 않았으므로
        // 어제부터 연속 일수를 계산
        LocalDate checkDate = attendToday
                ? today
                : today.minusDays(1);

        int currentStreak = 0;

        // 출석일이 끊기기 전까지 이전 날짜를 하루씩 확인
        while (attendanceDates.contains(checkDate)) {
            currentStreak++;
            checkDate = checkDate.minusDays(1);
        }

        return new AttendanceSummary(currentStreak, attendToday);
    }
}
