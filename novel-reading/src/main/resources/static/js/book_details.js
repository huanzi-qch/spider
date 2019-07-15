/**
 * 反防盗链
 */
function showImg(parentObj, url) {
    //来一个随机数
    var frameid = 'frameimg' + Math.random();
    //放在（父页面）window里面   iframe的script标签里面绑定了window.onload，作用：设置iframe的高度、宽度 <script>window.onload = function() {  parent.document.getElementById(\'' + frameid + '\').height = document.getElementById(\'img\').height+\'px\'; }<' + '/script>
    window.img = '<img src=\'' + url + '?' + Math.random() + '\'/>';
    //iframe调用parent.img
    $(parentObj).append('<iframe id="' + frameid + '" src="javascript:parent.img;" frameBorder="0" scrolling="no"></iframe>');
}

showImg($("#bookImg"), book.img);

$("body").on("click", ".click-book-read", function (even) {
    var chapterUrl = $(this).data("chapterurl");
    var refererUrl = $(this).data("refererurl");
    var sourceKey = $(this).data("sourcekey");
    window.location.href = ctx + "/book/read?sourceKey=" + sourceKey + "&refererUrl=" + refererUrl + "&chapterUrl=" + chapterUrl;
});