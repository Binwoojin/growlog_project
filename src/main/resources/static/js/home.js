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


