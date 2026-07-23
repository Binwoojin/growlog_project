<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>출석 현황 | GrowLog</title>
</head>

<body>

<%-- Attendance 페이지 연결 및 데이터 출력 테스트 영역 --%>
<h1>출석 현황</h1>

<p>
    현재 연속 출석:
    ${attendanceSummary.currentStreak}일
</p>

<c:choose>

    <%-- 오늘 목표 또는 성장기록을 등록해 출석이 인정된 경우 --%>
    <c:when test="${attendanceSummary.attendedToday}">
        <p>
            오늘 출석을 완료했어요.
        </p>
    </c:when>

    <%-- 오늘 아직 목표와 성장기록을 등록하지 않은 경우 --%>
    <c:otherwise>
        <p>
            오늘 목표나 성장기록을 등록하면 출석이 인정돼요.
        </p>
    </c:otherwise>

</c:choose>

</body>
</html>