<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>계정 확인 | GrowLog</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/common.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/settings-verify.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="settings_verify_page">

    <section class="settings_verify_card">

        <div class="settings_verify_icon">
            🔐
        </div>

        <span class="settings_verify_eyebrow">
            SECURITY CHECK
        </span>

        <h1>계정 정보를 확인해주세요</h1>

        <p>
            개인정보 보호를 위해 계정 설정에 들어가기 전
            현재 비밀번호를 다시 확인하고 있어요.
        </p>

        <c:if test="${not empty errorMessage}">
            <div class="verification_message error">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="verification_message success">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/mypage/settings/verify"
              method="post"
              class="settings_verify_form">

            <div class="verify_form_group">
                <label for="password">
                    현재 비밀번호
                </label>

                <input type="password"
                       id="password"
                       name="password"
                       autocomplete="current-password"
                       required
                       autofocus>

                <p>
                    로그인한 계정의 비밀번호를 입력해주세요.
                </p>
            </div>

            <div class="verify_form_actions">

                <a href="${pageContext.request.contextPath}/mypage/profile"
                   class="verify_cancel_button">
                    취소
                </a>

                <button type="submit"
                        class="verify_submit_button">
                    확인 후 이동
                </button>

            </div>

        </form>

    </section>

</main>

<script
        src="${pageContext.request.contextPath}/js/common.js">
</script>


</body>
</html>