package kr.co.growlog.growlog_project.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.growlog.growlog_project.dto.LoginRequest;
import kr.co.growlog.growlog_project.entity.Goal;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.service.GoalService;
import kr.co.growlog.growlog_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final MemberService memberService;
    // GoalService  추가
    private final GoalService goalService;

    // 로그인 페이지 이동
    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }


    // 로그인 처리
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Member loginMember = memberService.login(request);

            session.setAttribute("loginMember", loginMember);

            return "redirect:/home";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("loginErrorMessage", e.getMessage());

            return "redirect:/login";
        }


    }

    // 홈 페이지 이동
    @GetMapping("/home")
    public String homePage(HttpSession session,
                           Model model) { // 260720 Model 추가
        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        // goalCount 추가
        long goalCount = goalService.countGoalsByMember(loginMember.getMemberNo());

        // 홈에 최근 목표 리스트 추가
        List<Goal> recentGoals = goalService.findRecentGoalsByMember(loginMember.getMemberNo());
        // test
        System.out.println("홈 로그인 회원 번호: " + loginMember.getMemberNo());
        System.out.println("홈 최근 목표 목록: " + recentGoals);
        System.out.println("홈 최근 목표 개수: " + recentGoals.size());

        Member member = memberService.findById(loginMember.getMemberNo());

        model.addAttribute("recentGoals", recentGoals);
        model.addAttribute("goalCount", goalCount);
        model.addAttribute("loginMember", member);
        return "home";
    }


    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();

        return "logout";
    }
}
