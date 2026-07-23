<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>성장 기록 | GrowLog</title>

    <link rel="stylesheet" href="${contextPath}/css/common.css">
    <link rel="stylesheet" href="${contextPath}/css/record.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="record_list_page">

    <section class="record_list_hero">

        <div class="record_list_hero_text">

        <span class="record_list_hero_label">
            GROWTH RECORD
        </span>

            <h1 class="record_list_hero_title">
                매일의 경험이 모여<br>
                나만의 성장 과정이 됩니다.
            </h1>

            <p class="record_list_hero_description">
                지금까지 남긴 기록을 돌아보고<br>
                오늘의 새로운 이야기도 이어서 작성해보세요.
            </p>

        </div>


        <div class="record_list_hero_right">

            <img
                    src="${contextPath}/images/record_grobi.png"
                    alt="성장기록 그로비"
                    class="record_list_hero_image">

            <a href="${contextPath}/record/write"
               class="record_write_button">

                <span aria-hidden="true">＋</span>
                성장 기록 작성

            </a>

        </div>

    </section>

    <section class="record_list_content">

        <div class="record_list_heading">
            <div>
                <span class="record_list_label">MY RECORDS</span>
                <h2>나의 성장 기록</h2>
            </div>

            <p class="record_count">
                전체 <strong>${fn:length(records)}</strong>개의 기록
            </p>
        </div>

        <%-- ========================================
     성장 기록 필터
======================================== --%>

        <div class="record_filter_group"
             role="group"
             aria-label="성장 기록 필터">

            <%-- 전체 기록 보기 --%>
            <button
                    type="button"
                    class="record_filter_button active"
                    data-record-filter="ALL"
                    aria-pressed="true"
            >
                전체 보기
            </button>

            <%-- 목표 연동 기록만 보기 --%>
            <button
                    type="button"
                    class="record_filter_button"
                    data-record-filter="GOAL"
                    aria-pressed="false"
            >
                목표 연동 기록
            </button>

            <%-- 자유 기록만 보기 --%>
            <button
                    type="button"
                    class="record_filter_button"
                    data-record-filter="FREE"
                    aria-pressed="false"
            >
                자유 기록
            </button>

        </div>

        <c:choose>
            <c:when test="${empty records}">
                <section class="record_empty">
                    <div class="record_empty_icon" aria-hidden="true">🌱</div>

                    <h2>아직 작성한 성장 기록이 없어요.</h2>

                    <p>
                        오늘의 경험이나 감정, 새롭게 배운 내용을<br>
                        첫 성장 기록으로 남겨보세요.
                    </p>

                    <a href="${contextPath}/record/write" class="record_empty_button">
                        첫 기록 작성하기
                    </a>
                </section>
            </c:when>

            <c:otherwise>
                <div class="record_card_grid">
                    <c:forEach var="record" items="${records}">

                        <%-- 기록 유형 구분 --%>
                        <c:choose>
                            <c:when test="${record.goal != null}">
                                <c:set var="recordTypeValue" value="GOAL" />
                            </c:when>

                            <c:otherwise>
                                <c:set var="recordTypeValue" value="FREE" />
                            </c:otherwise>
                        </c:choose>

                        <%-- 성장 기록 카드 --%>
                        <article
                                class="record_card"
                                data-record-url="${contextPath}/record/${record.recordNum}"
                                data-record-type="${record.goal != null ? 'GOAL' : 'FREE'}"
                                tabindex="0"
                                role="link"
                                aria-label="${record.title} 상세보기"
                        >

                            <div class="record_card_top">
                                <c:choose>
                                    <c:when test="${record.goal != null}">
                                        <span class="record_type_badge goal_record">
                                            목표 연동 기록
                                        </span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="record_type_badge free_record">
                                            자유 기록
                                        </span>
                                    </c:otherwise>
                                </c:choose>

                                <time class="record_date">
                                    <c:out value="${fn:replace(fn:substring(record.createdAt, 0, 10), '-', '.')}" />
                                </time>
                            </div>

                            <div class="record_card_body">
                                <h3 class="record_title">
                                    <c:out value="${record.title}" />
                                </h3>

                                <p class="record_preview">
                                    <c:out value="${record.content}" />
                                </p>
                            </div>

                            <div class="record_card_bottom">
                                <c:if test="${not empty record.difficulty}">
                                    <span class="record_difficulty difficulty_${record.difficulty}">
                                        <c:choose>
                                            <c:when test="${record.goal != null}">
                                                난이도 · <c:out value="${record.difficulty}" />
                                            </c:when>

                                            <c:otherwise>
                                                오늘 기분 · <c:out value="${record.difficulty}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </c:if>

                                <a href="${contextPath}/record/${record.recordNum}"
                                   class="record_detail_link">
                                    자세히 보기
                                    <span aria-hidden="true">→</span>
                                </a>
                            </div>

                        </article>
                    </c:forEach>
                </div>

                <%-- 필터 결과 없음 --%>
                <div
                        class="record_filter_empty"
                        id="recordFilterEmpty"
                        hidden
                >
                    <div class="record_filter_empty_icon"
                         aria-hidden="true">
                        🌿
                    </div>

                    <p>
                        선택한 유형의 성장 기록이 없어요.
                    </p>
                </div>

            </c:otherwise>
        </c:choose>

    </section>

</main>

<script src="${contextPath}/js/common.js"></script>
<script src="${contextPath}/js/record.js"></script>
</body>
</html>