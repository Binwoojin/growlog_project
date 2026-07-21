<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>나의 목표 | GrowLog</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/common.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/goal.css">
</head>

<body class="goal_page">

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

        <a href="${pageContext.request.contextPath}/home"
           data-nav-link>
            홈
        </a>

        <a href="${pageContext.request.contextPath}/record/list"
           data-nav-link>
            성장 기록
        </a>

        <a href="${pageContext.request.contextPath}/goal/list"
           data-nav-link>
            목표
        </a>

        <a href="${pageContext.request.contextPath}/timeline"
           data-nav-link>
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

        <div class="profile_menu"
             id="profileMenu">

            <a href="#">마이페이지</a>

            <form
                    action="${pageContext.request.contextPath}/logout"
                    method="post"
                    class="logout_form"
            >
                <button
                        type="submit"
                        class="logout_button"
                        data-confirm="로그아웃하시겠습니까?"
                >
                    로그아웃
                </button>
            </form>

        </div>
    </div>

</header>

<main class="goal_container">

    <!-- ========================================
     Goal Page Header
======================================== -->

    <section class="goal_page_header">

        <!-- 헤더 왼쪽 콘텐츠 -->
        <div class="goal_header_content">

        <span class="goal_page_label">
            MY GOAL
        </span>

            <h1>
                나의 목표
            </h1>

            <p class="goal_header_description">
                이루고 싶은 목표를 정하고
                조금씩 성장하는 과정을 확인해 보세요.
            </p>

            <div class="goal_header_actions">

                <a
                        href="${pageContext.request.contextPath}/goal/write"
                        class="goal_primary_button"
                >
                <span class="goal_button_icon">
                    ＋
                </span>

                    새로운 목표 만들기
                </a>

                <span class="goal_header_hint">
                작은 목표부터 천천히 시작해 보세요 🌱
            </span>

            </div>

        </div>


        <!-- 헤더 오른쪽 캐릭터 영역 -->
        <div class="goal_header_character">

            <!-- 배경 장식 -->
            <span class="goal_deco_circle circle_one"></span>
            <span class="goal_deco_circle circle_two"></span>

            <img
                    src="${pageContext.request.contextPath}/images/goal_grobi.png"
                    alt="목표를 계획하는 그로비"
                    class="goal_grobi_image"
            >

        </div>

    </section>

    <c:if test="${not empty successMessage}">
        <div
                class="alert_message success"
                data-auto-close="3000"
        >
                ${successMessage}
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div
                class="alert_message error"
                data-auto-close="4000"
        >
                ${errorMessage}
        </div>
    </c:if>

    <section class="goal_list_panel">

        <div class="goal_list_header">
            <h2>등록한 목표</h2>

            <span class="goal_list_count">
                총 ${goal.size()}개의 목표
            </span>
        </div>

        <c:choose>

            <c:when test="${empty goal}">

                <div class="goal_empty">

                    <div class="goal_empty_icon">🌱</div>

                    <h2>아직 등록한 목표가 없어요</h2>

                    <p>
                        작은 목표부터 하나씩 등록해 보세요.<br>
                        GrowLog가 성장 과정을 함께 기록해 드릴게요.
                    </p>

                    <a
                            href="${pageContext.request.contextPath}/goal/write"
                            class="goal_primary_button"
                    >
                        첫 목표 만들기
                    </a>

                </div>

            </c:when>

            <c:otherwise>

                <!-- ========================================
                Goal Card List
                ======================================== -->

                <div class="goal_card_list">

                    <c:forEach var="goalItem"
                               items="${goal}">

                        <!-- ========================================
                        Goal Card
                        ======================================== -->

                        <article
                                class="goal_card"
                                tabindex="0"
                                role="button"
                                data-goal-card

                                data-goal-num="${goalItem.goalNum}"
                                data-goal-title="${goalItem.goalTitle}"
                                data-goal-content="${goalItem.goalContent}"
                                data-goal-progress="${goalItem.goalProgress}"
                                data-goal-status="${goalItem.goalStatus}"

                                data-goal-category-name="${goalItem.category.categoryName}"
                                data-goal-category-icon="${goalItem.category.categoryIcon}"
                                data-goal-category-color="${goalItem.category.categoryColor}"

                                data-goal-start-date="${goalItem.startDate}"
                                data-goal-end-date="${goalItem.endDate}"
                        >

                            <!-- 카드 상단: 카테고리와 진행률 -->
                            <div class="goal_card_header">

                                <!-- 목표 카테고리 -->
                                <span
                                        class="goal_category_badge"
                                        style="
                                                color: ${goalItem.category.categoryColor};
                                                background-color: ${goalItem.category.categoryColor}18;
                                                border-color: ${goalItem.category.categoryColor}40;
                                                "
                                >
                        <span class="goal_category_icon">
                                ${goalItem.category.categoryIcon}
                        </span>

                        <span>
                                ${goalItem.category.categoryName}
                        </span>
                    </span>

                                <!-- 목표 진행률 -->
                                <strong class="goal_progress_value">
                                        ${goalItem.goalProgress}%
                                </strong>

                            </div>


                            <!-- 카드 본문 -->
                            <div class="goal_card_body">

                                <div class="goal_title_area">

                                    <h3 class="goal_card_title">
                                            ${goalItem.goalTitle}
                                    </h3>

                                    <c:if test="${not empty goalItem.goalContent}">
                                        <p class="goal_description">
                                                ${goalItem.goalContent}
                                        </p>
                                    </c:if>

                                </div>

                                <!-- 목표 상태 -->
                                <c:choose>

                                    <c:when test="${goalItem.goalStatus eq '완료'}">
                            <span class="goal_status completed">
                                    ${goalItem.goalStatus}
                            </span>
                                    </c:when>

                                    <c:when test="${goalItem.goalStatus eq '중단'}">
                            <span class="goal_status paused">
                                    ${goalItem.goalStatus}
                            </span>
                                    </c:when>

                                    <c:otherwise>
                            <span class="goal_status">
                                    ${goalItem.goalStatus}
                            </span>
                                    </c:otherwise>

                                </c:choose>

                            </div>


                            <!-- 목표 진행률 막대 -->
                            <div
                                    class="goal_progress_bar"
                                    role="progressbar"
                                    aria-valuemin="0"
                                    aria-valuemax="100"
                                    aria-valuenow="${goalItem.goalProgress}"
                                    aria-label="${goalItem.goalTitle} 진행률"
                            >
                    <span
                            data-progress="${goalItem.goalProgress}"
                            style="
                                    background-color: ${goalItem.category.categoryColor};
                                    "
                    ></span>
                            </div>


                            <!-- 카드 하단 -->
                            <div class="goal_card_footer">

                                <!-- 날짜 정보 -->
                                <div class="goal_meta">

                                    <c:if test="${not empty goalItem.startDate}">
                            <span>
                                시작일 ${goalItem.startDate}
                            </span>
                                    </c:if>

                                    <c:if test="${not empty goalItem.endDate}">
                            <span>
                                종료일 ${goalItem.endDate}
                            </span>
                                    </c:if>

                                </div>


                                <!-- 수정 및 삭제 버튼 -->
                                <div class="goal_card_actions">

                                    <a
                                            href="${pageContext.request.contextPath}/goal/edit/${goalItem.goalNum}"
                                            class="goal_action_button edit"
                                    >
                                        수정
                                    </a>

                                    <form
                                            action="${pageContext.request.contextPath}/goal/delete/${goalItem.goalNum}"
                                            method="post"
                                            class="goal_delete_form"
                                            onsubmit="return confirm('이 목표를 정말 삭제하시겠습니까?');"
                                    >
                                        <button
                                                type="submit"
                                                class="goal_action_button delete"
                                        >
                                            삭제
                                        </button>
                                    </form>

                                </div>

                            </div>

                        </article>

                    </c:forEach>

                </div>

            </c:otherwise>

        </c:choose>

    </section>

</main>

<!-- ========================================
     Goal Detail Modal
======================================== -->

<div
        class="goal_modal"
        id="goalDetailModal"
        aria-hidden="true"
>
    <!-- 모달 바깥 배경 -->
    <div
            class="goal_modal_backdrop"
            data-modal-close
    ></div>

    <!-- 모달 내용 -->
    <section
            class="goal_modal_content"
            role="dialog"
            aria-modal="true"
            aria-labelledby="goalModalTitle"
    >

        <!-- 모달 닫기 버튼 -->
        <button
                type="button"
                class="goal_modal_close"
                data-modal-close
                aria-label="목표 상세 팝업 닫기"
        >
            ×
        </button>

        <!-- 카테고리 -->
        <div
                class="goal_modal_category"
                id="goalModalCategory"
        >
            <span id="goalModalCategoryIcon"></span>

            <span id="goalModalCategoryName"></span>
        </div>

        <!-- 제목 -->
        <h2
                class="goal_modal_title"
                id="goalModalTitle"
        ></h2>

        <!-- 내용 -->
        <p
                class="goal_modal_description"
                id="goalModalContent"
        ></p>

        <!-- 진행률 -->
        <div class="goal_modal_progress_header">

            <span>목표 진행률</span>

            <strong id="goalModalProgressText">
                0%
            </strong>

        </div>

        <div class="goal_modal_progress_bar">
            <span id="goalModalProgressBar"></span>
        </div>

        <!-- 상세 정보 -->
        <div class="goal_modal_info_grid">

            <div class="goal_modal_info_item">
                <span class="goal_modal_info_label">
                    목표 상태
                </span>

                <strong id="goalModalStatus"></strong>
            </div>

            <div class="goal_modal_info_item">
                <span class="goal_modal_info_label">
                    시작일
                </span>

                <strong id="goalModalStartDate"></strong>
            </div>

            <div class="goal_modal_info_item">
                <span class="goal_modal_info_label">
                    종료일
                </span>

                <strong id="goalModalEndDate"></strong>
            </div>

        </div>

        <!-- ========================================
             Modal Actions
        ======================================== -->

        <div class="goal_modal_actions">

            <!-- 삭제 -->
            <form
                    id="goalModalDeleteForm"
                    method="post"
                    class="goal_modal_delete_form"
                    onsubmit="return confirm('이 목표를 정말 삭제하시겠습니까?');"
            >
                <button
                        type="submit"
                        class="goal_modal_button delete"
                >
                    삭제하기
                </button>
            </form>

            <!-- 수정 -->
            <a
                    id="goalModalEditLink"
                    href="#"
                    class="goal_modal_button edit"
            >
                수정하기
            </a>

        </div>

    </section>

</div>



<script
        src="${pageContext.request.contextPath}/js/common.js">
</script>

<script
        src="${pageContext.request.contextPath}/js/goal.js">
</script>

</body>
</html>