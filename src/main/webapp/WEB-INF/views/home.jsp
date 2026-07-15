<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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

    <nav class="main_nav">
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
    </nav>

    <div class="profile_area">
        <button
                type="button"
                class="profile_button"
                aria-expanded="false"
                aria-controls="profileMenu"
        >
            그로비
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
                안녕하세요, 그로비님!<br>
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
            <strong>2</strong>
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

                <a href="#">전체 보기</a>
            </div>

            <div class="goal_item">
                <div class="goal_info">
                    <span class="goal_category">프로젝트</span>
                    <h3>GrowLog 웹 애플리케이션 완성</h3>
                    <p>AWS RDS와 Spring Boot를 활용한 프로젝트</p>
                </div>

                <strong>40%</strong>

                <div class="progress_bar">
                    <span style="width: 40%;"></span>
                </div>
            </div>

            <div class="goal_item">
                <div class="goal_info">
                    <span class="goal_category">공부</span>
                    <h3>Spring Boot 구조 이해하기</h3>
                    <p>Controller, Service, Repository 학습</p>
                </div>

                <strong>20%</strong>

                <div class="progress_bar">
                    <span style="width: 20%;"></span>
                </div>
            </div>
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
