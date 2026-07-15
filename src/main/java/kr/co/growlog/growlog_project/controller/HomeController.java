package kr.co.growlog.growlog_project.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.growlog.growlog_project.dto.LoginRequest;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final MemberService memberService;

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
    public String homePage(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }
        return "home";
    }


    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();

        return "logout";
    }
}
