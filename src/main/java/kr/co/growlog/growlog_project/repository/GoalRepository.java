package kr.co.growlog.growlog_project.repository;

import kr.co.growlog.growlog_project.entity.Goal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    // 특정 회원이 등록한 모든 목표를 최신순으로 조회
    @EntityGraph(attributePaths = "category")
    List<Goal> findByMemberMemberNoOrderByCreatedAtDesc(Long memberNo);

    // 홈 화면에 표시할 최근 목표를 최대 3개까지 조회
    @EntityGraph(attributePaths = "category")
    List<Goal> findTop3ByMemberMemberNoOrderByCreatedAtDesc(Long memberNo);

    // 특정 회원이 등록한 목표 개수 조회
    long countByMemberMemberNo(Long memberNo);

    // 목표 단건 조회
    @EntityGraph(attributePaths = "category")
    Optional<Goal> findByGoalNumAndMemberMemberNo(Long goalNum, Long memberNo);
}
