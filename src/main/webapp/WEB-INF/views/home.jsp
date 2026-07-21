<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">
    <title>GrowLog Home</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/home.css">
</head>
<body class="home_page">

<header class="main_header">
    <a href="${pageContext.request.contextPath}/home"
       class="header_logo">
        <img
                src="${pageContext.request.contextPath}/images/logo.png"
                alt="GrowLog"
        >
        <span>GrowLog</span>
    </a>

    <nav class="main_nav" id="mainNav">

        <a
                href="${pageContext.request.contextPath}/home"
                data-nav-link
        >
            홈
        </a>

        <a
                href="${pageContext.request.contextPath}/record/list"
                data-nav-link
        >
            성장 기록
        </a>

        <a
                href="${pageContext.request.contextPath}/goal/list"
                data-nav-link
        >
            목표
        </a>

        <a
                href="${pageContext.request.contextPath}/timeline"
                data-nav-link
        >
            타임라인
        </a>

    </nav>

    <div class="profile_area">
        <button
                type="button"
                class="profile_button"
                data-dropdown-button="profileMenu"
                aria-expanded="false"
                aria-controls="profileMenu"
        >
            ${sessionScope.loginMember.nickname}
        </button>

        <div class="profile_menu" id="profileMenu">
            <a href="#">마이페이지</a>
            <form
                    action="${pageContext.request.contextPath}/logout"
                    method="post"
                    class="logout_form"
            >
                <button
                        type="submit"
                        class="logout_button"
                >
                    로그아웃
                </button>
            </form>
        </div>
    </div>
</header>

<main class="home_container">

    <section class="welcome_section">
        <div>
            <span class="welcome_badge">TODAY'S GROWTH</span>

            <h1>
                안녕하세요, ${sessionScope.loginMember.nickname}님!<br>
                오늘도 한 걸음 성장해 볼까요?
            </h1>

            <p>
                목표를 확인하고 오늘의 경험과 배운 점을 기록해 보세요.
            </p>

            <div class="welcome_buttons">
                <button
                        type="button"
                        class="primary_button record_write_button"
                        data-url="${pageContext.request.contextPath}/record/write"
                >
                    성장 기록 작성
                </button>

                <button
                        type="button"
                        class="secondary_button goal_write_button"
                        data-url="${pageContext.request.contextPath}/goal/write"
                >
                    새로운 목표 만들기
                </button>
            </div>
        </div>

        <img
                src="${pageContext.request.contextPath}/images/grobi.png"
                alt="응원하는 그로비"
                class="home_growbi"
        >
    </section>

    <section class="summary_grid">
        <article class="summary_card">
            <span>진행 중인 목표</span>
            <strong>${goalCount}</strong>
            <p>이번 주 목표를 이어가고 있어요.</p>
        </article>

        <article class="summary_card">
            <span>이번 달 기록</span>
            <strong>7</strong>
            <p>꾸준히 기록하고 있어요.</p>
        </article>

        <article class="summary_card">
            <span>연속 기록</span>
            <strong>3일</strong>
            <p>오늘도 기록하면 4일째예요.</p>
        </article>
    </section>

    <section class="content_grid">

        <article class="content_panel">

            <div class="panel_header">
                <div>
                    <span class="panel_label">GOAL</span>
                    <h2>진행 중인 목표</h2>
                </div>

                <a href="${pageContext.request.contextPath}/goal/list">
                    전체 보기
                </a>
            </div>

            <c:choose>

                <c:when test="${empty recentGoals}">

                    <div class="goal_empty_message">
                        <p>아직 등록된 목표가 없습니다.</p>

                        <a href="${pageContext.request.contextPath}/goal/write">
                            목표 등록하기
                        </a>
                    </div>

                </c:when>

                <c:otherwise>

                    <c:forEach var="goalItem"
                               items="${recentGoals}">

                        <div class="goal_item">

                            <div class="goal_info">

                        <span class="goal_category">
                                ${goalItem.goalStatus}
                        </span>

                                <h3>
                                        ${goalItem.goalTitle}
                                </h3>

                                <c:if test="${not empty goalItem.goalContent}">
                                    <p>
                                            ${goalItem.goalContent}
                                    </p>
                                </c:if>

                            </div>

                            <strong>
                                    ${goalItem.goalProgress}%
                            </strong>

                            <div class="progress_bar">
                        <span
                                style="width: ${goalItem.goalProgress}%;"
                                aria-label="목표 진행률 ${goalItem.goalProgress}%"
                        ></span>
                            </div>

                        </div>

                    </c:forEach>

                </c:otherwise>

            </c:choose>

        </article>

        <article class="content_panel">
            <div class="panel_header">
                <div>
                    <span class="panel_label">RECORD</span>
                    <h2>최근 성장 기록</h2>
                </div>

                <a href="#">전체 보기</a>
            </div>

            <div class="record_item">
                <time datetime="2026-07-14">07.14</time>

                <div>
                    <h3>AWS RDS 연결 성공</h3>
                    <p>
                        DBeaver와 AWS RDS를 연결하고 MEMBER와
                        GOAL 테이블을 테스트했다.
                    </p>
                </div>
            </div>

            <div class="record_item">
                <time datetime="2026-07-14">07.14</time>

                <div>
                    <h3>MEMBER와 GOAL 관계 이해</h3>
                    <p>
                        PK와 FK를 이용해 1:N 관계를 직접 구성했다.
                    </p>
                </div>
            </div>
        </article>

    </section>

</main>

<script
        src="${pageContext.request.contextPath}/js/common.js">
</script>

<script
        src="${pageContext.request.contextPath}/js/home.js">
</script>


</body>
</html>
