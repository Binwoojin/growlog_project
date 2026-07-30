package kr.co.growlog.growlog_project.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.growlog.growlog_project.dto.AttendanceSummary;
import kr.co.growlog.growlog_project.entity.Goal;
import kr.co.growlog.growlog_project.entity.GrowthRecord;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.service.AttendanceService;
import kr.co.growlog.growlog_project.service.GoalService;
import kr.co.growlog.growlog_project.service.GrowthRecordService;
import kr.co.growlog.growlog_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final MemberService memberService;
    // GoalService  추가
    private final GoalService goalService;
    // GrowthRecordService 추가
    private final GrowthRecordService growthRecordService;
    // AttendenceServicde 추가
    private final AttendanceService attendanceService;


    // 로그인 페이지 이동
    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("loginMember") != null) {
            return "redirect:/home";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }



    // 홈 페이지 이동
    @GetMapping("/home")
    public String homePage(HttpSession session,
                           Model model) { // 260720 Model 추가
        // 세션에서 로그인한 회원 정보 조회
        Member loginMember = (Member) session.getAttribute("loginMember");

        // 로그인 정보가 없으면 로그인 페이지로 이동
        if (loginMember == null) {
            return "redirect:/login";
        }

        Long memberNo = loginMember.getMemberNo();

        // 이번 주에 등록된 진행 중 목표 개수 조회
        long thisWeekInProgressGoalCount = goalService.countThisWeekInProgressGoals(memberNo);

        // 이번 달에 작성된 성장 기록 개수
        long thisMonthRecordCount = growthRecordService.countThisMonthRecords(memberNo);

        // 최근 목표 조회
        List<Goal> recentGoals = goalService.findRecentGoalsByMember(memberNo);

        // 목표 등록일과 성장기록 등록일을 기준으로 출석 상태 계산
        AttendanceSummary attendanceSummary = attendanceService.getAttendanceSummary(memberNo);

        // 로그인한 회원이 최근에 작성한 성장기록 3개 조회
        List<GrowthRecord> recentRecords = growthRecordService.findRecentRecordsByMember(memberNo);


        model.addAttribute("goalCount", thisWeekInProgressGoalCount);
        model.addAttribute("thisMonthRecordCount", thisMonthRecordCount);
        model.addAttribute("attendanceSummary", attendanceSummary);
        model.addAttribute("recentRecords", recentRecords);
        model.addAttribute("recentGoals", recentGoals);
        model.addAttribute("loginMember", loginMember);
        return "home";
    }


    @GetMapping("/logout-complete")
    public String logoutCompletePage() {
        return "logout";
    }
}
