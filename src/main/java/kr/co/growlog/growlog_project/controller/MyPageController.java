package kr.co.growlog.growlog_project.controller;

// 마이페이지와 마이페이지 하위 메뉴의 요청을 처리하는 Controller

// 현재 구현하는 메뉴
// 1. 프로필 페이지
// 2. 출석 현황 페이지

// 이후 개인 정보 수정, 비밀번호 변경, 계정 관리 기능도
// 해당 Controller에 순서대로 추가 예정

import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;
import kr.co.growlog.growlog_project.dto.AttendanceSummary;
import kr.co.growlog.growlog_project.dto.NicknameUpdateRequest;
import kr.co.growlog.growlog_project.dto.PasswordUpdateRequest;
import kr.co.growlog.growlog_project.dto.SettingVerificationRequest;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.service.AttendanceService;
import kr.co.growlog.growlog_project.service.GoalService;
import kr.co.growlog.growlog_project.service.GrowthRecordService;
import kr.co.growlog.growlog_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.eclipse.tags.shaded.org.apache.xpath.operations.Bool;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MyPageController {

    // 목표 등록일과 성장 기록 등록일을 기준으로
    // 사용자의 출석 정보를 계산하는 Service
    private final AttendanceService attendanceService;

    private final MemberService memberService;
    private final GoalService goalService;
    private final GrowthRecordService growthRecordService;

    // 계정 설정 접근 인증 완료 여부를 저장할 세션키
    private static final String SETTING_VERIFIED_KEY = "ACCOUNT_SETTING_VERIFIED";

    // 계정 설정 인증 완료 시간을 저장할 세션 키
    private static final String SETTING_VERIFIED_AT_KEY = "ACCOUNT_SETTING_VERIFIED_AT";

    // 계정 설정 접근 인증 유효시간 : 10분
    private static final long SETTING_VERIFICATION_MINUTES = 10L;

    /**
     * /mypage 요청을 내 정보 화면으로 연결한다.
     */
    @GetMapping
    public String myPageRoot() {
        return "redirect:/mypage/profile";
    }


    // 마이페이지 기본 주소 요청을 프로필 페이지로 이동시킴

    // 사용자가 /mypage로 접속했을 때 빈 화면을 보여주지 않고
    // 마이페이지의 기본 메뉴인 프로필 페이지로 이동하도록 처리
    // @return 프로필 페이지 주소로 리다이렉트

    @GetMapping("/profile")
    public String myPage(HttpSession session,
                         Model model) {
        Member loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/login";
        }

        // 회원 번호를 기준으로 최신 회원 정보를 다시 조회
        Member member = memberService.findById(loginMember.getMemberNo());

        // 회원이 등록한 전체 목표 개수를 조회
        long goalCount = goalService.countGoalsByMember(member.getMemberNo());

        // 회원이 작성한 전체 성장 기록 개수를 조회
        long recordCount = growthRecordService.countRecordByMember(member.getMemberNo());

        AttendanceSummary attendanceSummary = attendanceService.getAttendanceSummary(member.getMemberNo());

        model.addAttribute("member", member);
        model.addAttribute("goalCount", goalCount);
        model.addAttribute("recordCount", recordCount);
        model.addAttribute("currentStreak", attendanceSummary.getCurrentStreak());

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

    // 계정 설정 페이지를 표시
    // 현재 로그인한 회원의 최신 정보를 조회하여
    // 닉네임과 이메일을 계정 설정 화면에 전달

    // @param session 현재 사용자의 로그인 세션
    // @param model JSP에 회원 정보를 전달하기 위한 객체
    // @return 계정 설정 화면 또는 로그인 페이지
    @GetMapping("/settings")
    public String settingPage(HttpSession session,
                              Model model) {
        Member loginMember = getLoginMember(session);

        if(loginMember == null) {
            return "redirect:/login";
        }

        // 비밀번호 재확인을 하지 않았거나 인증 유효시간이 지났다면 다시 확인 화면으로 이동
        if (!isSettingsVerified(session)) {
            return "redirect:/mypage/settings/verify";
        }

        // 세션에 저장된 정보는 로그인 시점의 정보일 수 있으므로
        // 회원 번호를 이용해 DB에서 최신 정보를 다시 조회
        Member member = memberService.findById(loginMember.getMemberNo());

        model.addAttribute("member", member);
        model.addAttribute("loginMember", member);

        return "mypage/settings";
    }

    // 계정 설정에 접근하기 전 비밀번호 재확인 화면을 표시
    @GetMapping("/settings/verify")
    public String settingVerificationPage(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        // 이미 최근 10분 안에 인증을 완료했다면 비밀번호 확인 페이지를 다시 거치지 않고 계정 설정 페이지로 이동
        if (isSettingsVerified(session)) {
            return "redirect:/mypage/settings";
        }

        return "mypage/settings-verify";
    }


    // 계정 설정 접근을 위한 현재 비밀번호 확인 요청을 처리
    @PostMapping("/settings/verify")
    public String verifySettingsPassword(@ModelAttribute SettingVerificationRequest request,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        Member loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/login";
        }

        boolean matched = memberService.matchesPassword(loginMember.getMemberNo(), request.getPassword());

        if (!matched) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");

            return "redirect:/mypage/settings/verify";
        }


        // 비밀번호 확인에 성공하면 계정 설정 접근 인증 상태와 인증 시간을 세션에 저장
        session.setAttribute(SETTING_VERIFIED_KEY, true);
        session.setAttribute(SETTING_VERIFIED_AT_KEY, LocalDateTime.now());

        return "redirect:/mypage/settings";
    }

    // 계정 설정 접근 인증이 아직 유효한지 확인
    // 인증 여부가 true이고
    // 인증 완료 후 10분이 지나지 않은 경우에만 true를 반환

    private boolean isSettingsVerified(HttpSession session) {
        Boolean verified = (Boolean) session.getAttribute(SETTING_VERIFIED_KEY);

        LocalDateTime verifiedAt = (LocalDateTime) session.getAttribute(SETTING_VERIFIED_AT_KEY);

        if (!Boolean.TRUE.equals(verified) || verifiedAt == null) {
            return false;
        }

        long elapsedMinutes = Duration.between(verifiedAt,LocalDateTime.now()).toMinutes();

        // 인증 시간이 만료되면 세션에 남은 인증 정보도 함께 제거한다
        if (elapsedMinutes >= SETTING_VERIFICATION_MINUTES) {
            clearSettingsVerification(session);

            return false;
        }

        return true;
    }

    // 계정 설정 화면에서 닉네임 변경 요청을 처리
    @PostMapping("/settings/nickname")
    public String updateNickname(@ModelAttribute NicknameUpdateRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Member loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/login";
        }

        if (!isSettingsVerified(session)) {
            return "redirect:/mypage/settings/verify";
        }

        try {
            Member updateMember = memberService.updateNickname(loginMember.getMemberNo(), request);

            // 헤더에서 세션 회원의 닉네임을 사용하고 있으므로 변경된 객체로 로그인 세션을 갱신
            session.setAttribute("loginMember", updateMember);
            redirectAttributes.addFlashAttribute("successMessage", "닉네임이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("nicknameError", e.getMessage());
        }
        return "redirect:/mypage/settings";
    }

    // 계정 설정 화면에서 비밀번호 변경 요청을 처리
    @PostMapping("/settings/password")
    public String updatePassword(@ModelAttribute PasswordUpdateRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Member loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/login";
        }

        if (!isSettingsVerified(session)) {
            return "redirect:/mypage/settings/verify";
        }

        try {
            Member updatedMember = memberService.updatePassword(loginMember.getMemberNo(), request);
            session.setAttribute("loginMember", updatedMember);

            // 비밀번호 변경 후에는 계정 설정 접근 인증을 제거한다
            // 다시 설정 페이지에 접근할 떄 새 비밀번호로 인증하게 된다.
            clearSettingsVerification(session);

            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다. 새 비밀번호로 다시 확인해주세요.");

            return "redirect:/mypage/settings/verify";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());

            return "redirect:/mypage/settings";
        }
    }






    // 계정 설정 접근 인증 정보를 세션에서 제거
    private void clearSettingsVerification(HttpSession session) {
        session.removeAttribute(SETTING_VERIFIED_KEY);
        session.removeAttribute(SETTING_VERIFIED_AT_KEY);
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
