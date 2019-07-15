var nums = 10; //每页出现的数量
var pages = books.length; //总数

/**
 * 传入当前页，根据nums去计算，从books集合截取对应数据做展示
 */
var thisDate = function (curr) {
    var str = "",//当前页需要展示的html
        first = (curr * nums - nums),//展示的第一条数据的下标
        last = curr * nums - 1;//展示的最后一条数据的下标
    last = last >= books.length ? (books.length - 1) : last;
    for (var i = first; i <= last; i++) {
        var book = books[i];
        str += "<div class='book'>" +
            "<img class='click-book-detail' data-bookurl='" + book.bookUrl + "' data-sourcekey='" + book.source.key + "' data-searchurl='" + book.source.searchUrl + "' src='" + book.img + "'></img>" +
            "<p class='click-book-detail' data-bookurl='" + book.bookUrl + "' data-sourcekey='" + book.source.key + "' data-searchurl='" + book.source.searchUrl + "'>书名：" + book.bookName + "</p>" +
            "<p>作者：" + book.author + "</p>" +
            "<p>简介：" + book.synopsis + "</p>" +
            "<p class='click-book-read' data-chapterurl='" + book.latestChapterUrl + "' data-sourcekey='" + book.source.key + "' data-refererurl='" + book.source.refererurl + "'>最新章节：" + book.latestChapter + "</p>" +
            "<p>更新时间：" + book.updateDate + "</p>" +
            "<p>大小：" + book.magnitude + "</p>" +
            "<p>状态：" + book.status + "</p>" +
            "<p>类型：" + book.type + "</p>" +
            "<p>来源：" + book.source.name + "</p>" +
            "</div><br/>";
    }
    return str;
};

//获取一个laypage实例
layui.use('laypage', function () {
    var laypage = layui.laypage;

    //调用laypage 逻辑分页
    laypage.render({
        elem: 'page',
        count: pages,
        limit: nums,
        jump: function (obj) {
            //obj包含了当前分页的所有参数，比如：
            // console.log(obj.curr); //得到当前页，以便向服务端请求对应页的数据。
            // console.log(obj.limit); //得到每页显示的条数
            document.getElementById('books').innerHTML = thisDate(obj.curr);
        },
        prev: '<',
        next: '>',
        theme: '#f9c357',
    })
});

$("body").on("click", ".click-book-detail", function (even) {
    var bookUrl = $(this).data("bookurl");
    var searchUrl = $(this).data("searchurl");
    var sourceKey = $(this).data("sourcekey");
    window.location.href = ctx + "/book/details?sourceKey=" + sourceKey + "&searchUrl=" + searchUrl + "&bookUrl=" + bookUrl;
});
$("body").on("click", ".click-book-read", function (even) {
    var chapterUrl = $(this).data("chapterurl");
    var refererUrl = $(this).data("refererurl");
    var sourceKey = $(this).data("sourcekey");
    window.location.href = ctx + "/book/read?sourceKey=" + sourceKey + "&refererUrl=" + refererUrl + "&chapterUrl=" + chapterUrl;
});