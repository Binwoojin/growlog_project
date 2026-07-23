package kr.co.growlog.growlog_project.dto;

// 성장 기록 등록 및 수정 요청 DTO

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GrowthRecordRequest {

    private Long goalNum; // 연결할 목표 번호 | 목표와 연결하지 않는 자유 기록인 경우는 NULL이다.
    private String title; // 성장 기록 제목
    private String content; // 성장 기록 본문
    private String todayLearning; // 오늘 배운 내용
    private String difficulty; // 체감 난이도
    private String solution; // 문제 해결 과정
    private String retrospective; // 최종 회고

}
