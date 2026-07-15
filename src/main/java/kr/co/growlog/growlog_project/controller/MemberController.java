package kr.co.growlog.growlog_project.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.growlog.growlog_project.dto.DuplicateCheckResponse;
import kr.co.growlog.growlog_project.dto.JoinRequest;
import kr.co.growlog.growlog_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;


    // 회원가입 페이지 이동
    @GetMapping("/join")
    public String joinPage() {
        return "member/join";
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String join(@ModelAttribute JoinRequest request,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {

        memberService.join(request, session);

        redirectAttributes.addFlashAttribute("joinSuccessMessage", "회원가입이 완료되었습니다.");

        return "redirect:/login";

    }


    // 닉네임 중복확인 API
    @ResponseBody
    @GetMapping("/api/members/check-nickname")
    public DuplicateCheckResponse checkNickname(@RequestParam("nickname") String nickname) {
        boolean duplicated = memberService.isNicknameDuplicated(nickname);

        if (duplicated) {
            return new DuplicateCheckResponse(true, "이미 사용 중인 닉네임입니다.");
        }

        return new DuplicateCheckResponse(false, "사용 가능한 닉네임입니다.");
    }
}
