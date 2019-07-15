$("body").on("click", ".click-book-read", function (even) {
    var chapterUrl = $(this).data("chapterurl");
    var refererUrl = $(this).data("refererurl");
    var sourceKey = $(this).data("sourcekey");
    window.location.href = ctx + "/book/read?sourceKey=" + sourceKey + "&refererUrl=" + refererUrl + "&chapterUrl=" + chapterUrl;
});