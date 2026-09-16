package kr.co.growlog.growlog_project.controller;

import kr.co.growlog.growlog_project.dto.GrowthRecordResponse;
import kr.co.growlog.growlog_project.entity.GrowthRecord;
import kr.co.growlog.growlog_project.security.LoginMemberPrincipal;
import kr.co.growlog.growlog_project.service.GrowthRecordService;
import kr.co.growlog.growlog_project.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * Vue Record Detail 화면(Timeline → Record 카드 클릭 → /record/:id)이
 * 사용하는 API.
 *
 * 새 비즈니스 로직을 추가하지 않는다 — 기존 JSP용 GrowthRecordController.
 * recordDetail()과 동일한 GrowthRecordService.findRecordById() /
 * MediaService.findMediaByGrowthRecord()를 그대로 재사용해서 JSON으로만
 * 다시 노출한다. 다른 회원의 기록 접근 차단도 findRecordById()가 이미
 * memberNo로 검증하므로 여기서 중복 구현하지 않는다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class GrowthRecordApiController {

    private final GrowthRecordService growthRecordService;
    private final MediaService mediaService;

    @GetMapping("/records/{recordNum}")
    public GrowthRecordResponse detail(
            @PathVariable Long recordNum,
            @AuthenticationPrincipal LoginMemberPrincipal principal) {
        GrowthRecord record = growthRecordService.findRecordById(recordNum, principal.getMemberNo());
        return GrowthRecordResponse.from(record, mediaService.findMediaByGrowthRecord(recordNum));
    }

    /*
     * findRecordById()는 기록이 없거나 다른 회원의 기록일 때 동일하게
     * IllegalArgumentException("성장 기록을 찾을 수 없습니다.")을 던진다.
     * GoalApiController와 같은 패턴으로 400 + 메시지로 응답해서 프론트가
     * Not Found 상태를 그대로 표시할 수 있게 한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
