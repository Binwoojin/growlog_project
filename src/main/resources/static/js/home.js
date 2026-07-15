"use strict";

document.addEventListener("DOMContentLoaded", () => {
    initProgressBars();
});

function initProgressBars() {

    const progressBars =
        document.querySelectorAll(".progress_bar span");

    progressBars.forEach((bar) => {

        const width = bar.style.width;

        bar.style.width = "0";

        requestAnimationFrame(() => {

            requestAnimationFrame(() => {

                bar.style.width = width;

            });

        });

    });


}

document.addEventListener("DOMContentLoaded", () => {
    initProfileMenu();
});

function initProfileMenu() {
    const profileArea = document.querySelector(".profile_area");
    const profileButton = document.querySelector(".profile_button");
    const profileMenu = document.querySelector("#profileMenu");

    if (!profileArea || !profileButton || !profileMenu) {
        return;
    }

    profileButton.addEventListener("click", (event) => {
        event.stopPropagation();

        const isOpen = profileArea.classList.toggle("open");

        profileButton.setAttribute(
            "aria-expanded",
            String(isOpen)
        );
    });

    // 메뉴 내부 클릭 시 바로 닫히지 않게 처리
    profileMenu.addEventListener("click", (event) => {
        event.stopPropagation();
    });

    // 메뉴 바깥을 클릭하면 닫기
    document.addEventListener("click", () => {
        closeProfileMenu();
    });

    // ESC 키를 누르면 닫기
    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeProfileMenu();
            profileButton.focus();
        }
    });

    function closeProfileMenu() {
        profileArea.classList.remove("open");
        profileButton.setAttribute("aria-expanded", "false");
    }
}

