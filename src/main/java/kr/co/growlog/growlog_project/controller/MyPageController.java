package kr.co.growlog.growlog_project.controller;

// 마이페이지와 마이페이지 하위 메뉴의 요청을 처리하는 Controller

// 현재 구현하는 메뉴
// 1. 프로필 페이지
// 2. 출석 현황 페이지

// 이후 개인 정보 수정, 비밀번호 변경, 계정 관리 기능도
// 해당 Controller에 순서대로 추가 예정

import jakarta.servlet.http.HttpSession;
import kr.co.growlog.growlog_project.dto.AttendanceSummary;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MyPageController {

    // 목표 등록일과 성장 기록 등록일을 기준으로
    // 사용자의 출석 정보를 계산하는 Service
    private final AttendanceService attendanceService;



    // 마이페이지 기본 주소 요청을 프로필 페이지로 이동시킴

    // 사용자가 /mypage로 접속했을 때 빈 화면을 보여주지 않고
    // 마이페이지의 기본 메뉴인 프로필 페이지로 이동하도록 처리
    // @return 프로필 페이지 주소로 리다이렉트

    @GetMapping
    public String myPage() {
        return "redirect:/mypage/profile";
    }

    // 마이 페이지의 기본 프로필 화면을 표시
    // 세션에 저장된 로그인 회원을 확인한 뒤
    // 로그인한 회원 정보만 JSP로 전달

    // 현재는 조회 화면만 구현하고
    // 개인정보 수정 기능은 별도의 페이지로 분리할 예정

    // @param session 현재 사용자의 로그인 세션
    // @param model JSP에 데이터를 전달하기 위한 객체
    // @return 프로필 화면 또는 로그인 페이지
    @GetMapping("/profile")
    public String profilePage(HttpSession session,
                              Model model) {

        // 로그인 성공 시 세션에 저장했던 회원 정보를 가져온다
        // GrowLog에서 사용 중인 세션 키가 "loginMember"이므로
        // 로그인, 홈, 목표, 기록 페이지와 동일한 키를 사용
        Member loginMember = getLoginMember(session);

        // 세션에 로그인 회원이 없다면
        // 비로그인 사용자가 마이페이지 주소로 직접 접근한 경우이므로
        // 로그인 페이지로 이동시킴
        if (loginMember == null) {
            return "redirect:/login";
        }

        // profile.jsp에서 닉네임, 이메일 등의 회원 정보를
        // 출력할 수 있도록 로그인 회원 객체를 Model에 저장
        model.addAttribute("loginMember", loginMember);

        return "mypage/profile";
    }

    // 마이페이지 하위 메뉴인 출석 현황 화면을 표시

    // 출석 인정 조건
    // 1. 해당 날짜에 목표를 1개 이상 등록했거나
    // 2. 해당 날짜에 성장기록을 1개 이상 등록한 경우

    // 현재 단계에서는 기존 AttendanceService를 이용해
    // 오늘 출석 여부와 현재 연속 출석 일수를 전달

    // @param session 현재 사용자의 로그인 세션
    // @param model JSP에 출석 정보를 전달하기 위한 객체
    // @return 출석 현황 화면 또는 로그인 페이지
    @GetMapping("/attendance")
    public String attendancePage(HttpSession session,
                                 Model model) {

        // 세션에서 현재 로그인한 회원을 조회
        Member loginMember = getLoginMember(session);

        // 로그인하지 않은 사용자는 자신의 출석 정보를 조회할 수 없으므로
        // 로그인 페이지로 이동
        if ( loginMember == null ) {
            return "redirect:/login";
        }

        // 로그인 회원 번호를 이용해 출석 요약 정보를 계산
        // AttendanceSummary에서 현재 기준으로
        // currentStreak와 attendedToday가 들어있음
        AttendanceSummary attendanceSummary = attendanceService.getAttendanceSummary(loginMember.getMemberNo());

        // 마이페이지 공통 프로필 영역이나 사이드바에서
        // 회원 정보를 사용할 수 있도록 로그인 회원을 전달
        model.addAttribute("loginMember", loginMember);

        // attendance.jsp에서 현재 연속 출석 일수와
        // 오늘 출석 완료 여부를 출력할 수 있도록 전달
        model.addAttribute("attendanceSummary", attendanceSummary);

        return "mypage/attendance";
    }

    // 세션에서 로그인 회원 객체를 가져오는 공통 메서드

    // 프로필, 출석 현황, 개인정보 수정 등 마이페이지의 모든 메뉴는
    // 로그인 검증이 필요하므로 동일한 세션 조회 코드를
    // 각 메서드마다 반복하지 않도록 별도의 메서드를 분리

    // @param session 현재 사용자의 세션
    // @return 로그인 회원 객체, 로그인 정보가 없으면 null
    private Member getLoginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");
    }
}
