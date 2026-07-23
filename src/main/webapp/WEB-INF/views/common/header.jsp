<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- ========================================
Common Header
======================================== -->

<header class="main_header">

    <div class="header_inner">

        <!-- ========================================
             Logo
        ======================================== -->

        <a
                href="${pageContext.request.contextPath}/home"
                class="header_logo"
        >
            <img
                    src="${pageContext.request.contextPath}/images/logo.png"
                    alt=""
            >

            <span>
                GrowLog
            </span>
        </a>


        <!-- ========================================
             Main Navigation
        ======================================== -->

        <nav
                class="main_nav"
                id="mainNav"
                aria-label="주요 메뉴"
        >
            <a
                    href="${pageContext.request.contextPath}/home"
                    data-nav-link
                    data-nav-prefix="/home"
            >
                홈
            </a>

            <a
                    href="${pageContext.request.contextPath}/record/list"
                    data-nav-link
                    data-nav-prefix="/record"
            >
                성장 기록
            </a>

            <a
                    href="${pageContext.request.contextPath}/goal/list"
                    data-nav-link
                    data-nav-prefix="/goal"
            >
                목표
            </a>

            <a
                    href="${pageContext.request.contextPath}/timeline"
                    data-nav-link
                    data-nav-prefix="/timeline"
            >
                타임라인
            </a>
        </nav>


        <!-- ========================================
             Header Actions
        ======================================== -->

        <div class="header_actions">

            <!-- 모바일 메뉴 버튼 -->
            <button
                    type="button"
                    class="mobile_menu_button"
                    data-mobile-menu-button="mainNav"
                    aria-expanded="false"
                    aria-controls="mainNav"
                    aria-label="메뉴 열기"
            >
                <span></span>
                <span></span>
                <span></span>
            </button>


            <!-- 프로필 -->
            <div class="profile_area">

                <button
                        type="button"
                        class="profile_button"
                        data-dropdown-button="profileMenu"
                        aria-expanded="false"
                        aria-controls="profileMenu"
                >
                    <span class="profile_avatar">
                        ${sessionScope.loginMember.nickname.substring(0, 1)}
                    </span>

                    <span class="profile_name">
                        ${sessionScope.loginMember.nickname}
                    </span>

                    <span
                            class="profile_arrow"
                            aria-hidden="true"
                    >
                        ▾
                    </span>
                </button>


                <!-- 프로필 드롭다운 -->
                <div
                        class="profile_menu"
                        id="profileMenu"
                >
                    <div class="profile_menu_header">

                        <strong>
                            ${sessionScope.loginMember.nickname}
                        </strong>

                        <span>
                            ${sessionScope.loginMember.email}
                        </span>

                    </div>

                    <div class="profile_menu_divider"></div>

                    <a
                            href="${pageContext.request.contextPath}/mypage/profile"
                            class="profile_menu_link"
                    >
                        마이페이지
                    </a>

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

        </div>

    </div>

</header>