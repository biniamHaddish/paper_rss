var webViewJsBridge = window.web_view_bridge;
var articleDownloadHtml = '<img id="loader" src="article_download_animation.svg"/><div id="message"><p class="header"></p><p class="second"></p></div><button id="show_orignal" type="button" class="show_org btn"><span></span></button>'

var loadingIcon = "article_download_animation.svg";
var errorIcon = "article_download_error.svg";
var originalHtml;
var timer;

$(document).ready(function (){

    PR.prettyPrint();

    var lastScrollTop = 0;
    var docHeight = $(document).height();
    var windowHeight = $(window).height();

    var touchstartTime;
    var touchendTime;

    originalHtml = $('#main').html();

    // Set relative time of the feed
    var timestamp = $('#timeinfo').attr("timestamp");
    $('#timeinfo').text(webViewJsBridge.getRelativeTime(timestamp));


    $('#main').on('click', 'img', function (e){
        webViewJsBridge.setIgnoreFullScreen(true)
        var id = $(this).attr('id');
        var org_src = $(this).attr('org_src');
        var src = $(this).attr('src');

        if (id != "loader") {
            var downloaded = $(this).attr("img-downloaded");


            if (downloaded == "true"){
                webViewJsBridge.showDownloadedImage(id);
            } else {
                webViewJsBridge.downloadImage(id, org_src, src);
            }
        }
        e.preventDefault();
        e.stopPropagation();
    });

    //$('#main').on('click', '.show_org', function(){

    $('body').click(function (e){
        webViewJsBridge.setIgnoreFullScreen(false)
    });



//    $('#main').dblclick(function(e){
//            showFullArticleDownloadingHtml()
//            webViewJsBridge.downloadFullArticle();
//            console.log('article content double clicked');
//    })


     // Handle link clicks
     $('#main').on('click', 'a', function (e) {
        e.preventDefault();
        webViewJsBridge.setIgnoreFullScreen(true)
        var url = $(this).attr('href');
        webViewJsBridge.onLinkClicked(url)
        e.stopPropagation();
    });

     // Handle youtube clicks
     $('#main').on('click', '.utube', function (e) {
            e.preventDefault();
            webViewJsBridge.setIgnoreFullScreen(true)
            var url = $(this).attr('iframe_src');
            webViewJsBridge.openYoutubeVideo(url)
            e.stopPropagation();
    });

    $('#main').on('touchstart', 'a', function (e) {
        webViewJsBridge.setIgnoreFullScreen(true)
        var url = $(this).attr('href');
        // Save the position that started this event
        var startPosition = webViewJsBridge.getPagePosition();
        timer = setTimeout(function (e) {
            var currentPosition = webViewJsBridge.getPagePosition();
            // Only show the dialog if the users hasn't switched from the page that started it
            if (startPosition == currentPosition){
                webViewJsBridge.onLinkLongClicked(url)
            }
        }, 500)
        e.stopPropagation();
    })


    $('#main').on('touchstart', 'pre', function (e) {
         console.log("code -> " + $(this).text())
    })



    $('#main').on('click', '.generic', function(e){
        webViewJsBridge.setIgnoreFullScreen(true)
        var url = $(this).attr('iframe_src');
        webViewJsBridge.onLinkClicked(url);
        e.stopPropagation();
    });

    $('#main').on('touchend touchmove touchcancelled', 'a', function(e){
        webViewJsBridge.setIgnoreFullScreen(true)
        clearTimeout(timer);
        e.stopPropagation();
    });

})

function setImage(id, src){
    $(id).attr("src", src)
    originalHtml = $('#main').html();
}

function setBackgroundColor(bgColor, textColor, linkColor){
    $('body').css({'background-color': bgColor})
    $('#title').css({'color': textColor})
    $('#title').css({'border-bottom-color': textColor})
    $('#main').css({'color': textColor})
    $('a').css({'color': linkColor})
    $('#sourceinfo').css({'color': textColor})
    $('#timeinfo').css({'color': textColor})
}


function updateFont(fontName){

    $('#title').css({'font-family': fontName + " Bold"})
    $('#main').css({'font-family': fontName + " Regular"})
    $('#sourceinfo').css({'font-family': fontName + " Regular"})
    $('#timeinfo').css({'font-family': fontName + " Regular"})
    $('strong, h1, h2, h3, h4, h5').css({'font-family': fontName + " Bold"})
    $('em, i, abbr').css({'font-family': fontName + " Italic"})
}

function updateFontSize(titleFontSize, contentFontSize, articleInfoFontSize){
    $('#title').css({'font-size': titleFontSize + "px"})
    $('#main').css({'font-size': contentFontSize + "px"})
    $('#sourceinfo').css({'font-size': articleInfoFontSize + "px"})
    $('#timeinfo').css({'font-size': articleInfoFontSize + "px"})
}


function updateLineHeight(lineHeight){
    $('#title').css({'line-height': lineHeight})
    $('#main').css({'line-height': lineHeight})
}

function updateJustification(justification){
    //$('#title').css({'text-align': justification})
    $('#main').css({'text-align': justification})
}


function showFullArticleDownloadingHtml(){
     $('#main').html(articleDownloadHtml);
     $('#loader').attr('src', loadingIcon);
     $('#message .header').css({ 'display' : 'none'});
     $('#message .second').text(webViewJsBridge.getArticleDownloadingString());
     $('#show_orignal').css({ 'display' : 'none'});
     $('#message .header').css({ 'display' : 'none'});
     $('#retry').css({ 'display' : 'none'});

}

function showErrorDownloadingFullArticleHtml(){
    $('#main').html(articleDownloadHtml);
    $('#loader').attr('src', errorIcon);
    $('#message .header').text(webViewJsBridge.getArticleDownloadErrorString());
    $('#message .second').text(webViewJsBridge.getDoubleTapToRetryString());
    $('#message .header').css({ 'display' : 'visible'});
    $('#message .second').css({ 'display' : 'visible'});
    $('#show_orignal').css({ 'display' : 'visible'});
    $('#retry').css({ 'display' : 'visible'});
    $('#show_orignal span').text(webViewJsBridge.getShowOriginalString());
    $('#retry span').text(webViewJsBridge.getRetryString());

    $('#main').on('click', '.show_org', function(e){
        webViewJsBridge.setIgnoreFullScreen(true)
        $('#main').html(originalHtml)
        e.stopPropagation();
    })
}