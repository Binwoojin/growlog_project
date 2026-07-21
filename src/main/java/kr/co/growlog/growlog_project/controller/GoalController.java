package kr.co.growlog.growlog_project.controller;


import jakarta.servlet.http.HttpSession;
import kr.co.growlog.growlog_project.dto.GoalRequest;
import kr.co.growlog.growlog_project.entity.Category;
import kr.co.growlog.growlog_project.entity.Goal;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/goal")
public class GoalController {

    private final GoalService goalService;

    // 목표 목록 페이지
    @GetMapping("/list")
    public String goalList(HttpSession session,
                           Model model) {
        Member loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Goal> goal = goalService.findGoalsByMember(loginMember.getMemberNo());

        // 테스트

        System.out.println("로그인 회원 번호: "
                + loginMember.getMemberNo());

        System.out.println("조회된 목표 개수: "
                + goal.size());

        for (Goal item : goal) {
            System.out.println("목표 제목: " + item.getGoalTitle());
            System.out.println("목표 상태: [" + item.getGoalStatus() + "]");
            System.out.println("목표 진행률: [" + item.getGoalProgress() + "]");
        }

        model.addAttribute("goal", goal);

        return "goal/list";

    }

    // 목표 등록 화면
    @GetMapping("/write")
    public String goalWriteForm(HttpSession session,
                                Model model) {
        // 세션에서 로그인한 회원 조회
        Member loginMember = getLoginMember(session);

        // 로그인하지 않은 경우 로그인 페이지로 이동
        if (loginMember == null) {
            return "redirect:/login";
        }

        // ========================================
        // 카테고리 조회 결과 확인
        // ========================================

        List<Category> categories =
                goalService.findAllCategories();

        // 카테고리가 몇 개 조회되는지 콘솔에서 확인
        System.out.println(
                "조회된 카테고리 개수: "
                        + categories.size()
        );

        // 조회된 카테고리 내용을 하나씩 출력
        for (Category category : categories) {
            System.out.println(
                    "카테고리 번호: "
                            + category.getCategoryNum()
                            + ", 카테고리 이름: "
                            + category.getCategoryName()
            );
        }


        // 목표 등록 화면에서 사용할 카테고리 목록 전달
        model.addAttribute("categories", goalService.findAllCategories());

        return "goal/write";
    }

    // 목표 등록 처리
    @PostMapping("/write")
    public String saveGoal(@ModelAttribute GoalRequest request,
                           HttpSession session) {
        Member loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/login";
        }

        goalService.saveGoal(loginMember.getMemberNo(), request);

        return "redirect:/goal/list";
    }



    // 목표 수정 페이지

    @GetMapping("/edit/{goalNum}")
    public String goalEditPage(@PathVariable("goalNum") Long goalNum,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // 세션에서 로그인한 회원 정보 조회
        Member loginMember = getLoginMember(session);

        // 로그인하지 않은 경우 로그인 페이지로 이동
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            // 수정할 목표 조회
            Goal goal = goalService.findGoalById(goalNum, loginMember.getMemberNo());

            // edit.jsp에서 기존 목표 데이터를 사용할 수 있도록 전달
            model.addAttribute("goal", goal);
            // 수정 화면에서 선택할 카테고리 목록 전달
            model.addAttribute("categories", goalService.findAllCategories());

            return "goal/edit";
        } catch (IllegalArgumentException e) {
            // 목표가 없거나 다른 회원의 목표인 경우
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/goal/list";
        }

    }

    // 수정 처리

    @PostMapping("/edit/{goalNum}")
    public String updateGoal(@PathVariable("goalNum") Long goalNum,
                             @ModelAttribute GoalRequest request,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        // 세션에서 로그인한 회원 정보 조회
        Member loginMember = getLoginMember(session);

        // 로그인하지 않은 경우 로그인 페이지로 이동
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            // 목표 수정 Service 호출
            goalService.updateGoal(goalNum, loginMember.getMemberNo(), request);

            // 목표 목록 페이지에서 출력할 성공 메시지
            redirectAttributes.addFlashAttribute("successMessage", "목표가 성공작으로 수정되었습니다.");

            return "redirect:/goal/list";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/goal/edit/" + goalNum;

        }
    }

    // 목표 삭제 처리
    // 목표 삭제 요청을 처리
    // POST /goal/delete/{goalNum}

    @PostMapping("/delete/{goalNum}")
    public String deleteGoal(@PathVariable("goalNum") Long goalNum,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // 세션에서 로그인한 회원 정보 조회
        Member loginMember = getLoginMember(session);

        // 로그인하지 않은 경우 로그인 페이지로 이동
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            // 현재 로그인한 회원의 목표 삭제
            goalService.deleteGoal(goalNum, loginMember.getMemberNo());

            // 삭제 성공 메시지 전달
            redirectAttributes.addFlashAttribute("successMessage", "목표가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            // 목표가 없거나 삭제 권한이 없는 경우
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/goal/list";
    }

    // 현재 로그인한 회원 정보를 세션에서 조회함
    // 로그인 성공 시, HomeController에서 "LoginMember"라는 이름으로
    // 세션에 저장한 Member 객체를 반환

    // Controller 내에서 동일한 세션 조회 코드를 반복 작성하지 않도록
    // 공통 메서드로 분리

    private Member getLoginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");
    }

}
