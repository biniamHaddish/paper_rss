package com.biniam.rss.ui.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Keep;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.biniam.rss.R;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.preferences.ReadingPrefs;
import com.biniam.rss.utils.CSSConstants;
import com.biniam.rss.utils.PaperApp;
import com.biniam.rss.utils.TemplateExtractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.HttpUrl;

/**
 * Created by biniam_Haddish on 8/1/17.
 * <p>
 * This class intercepts request made by a {@link WebView} and returns appropriate content based on
 * the request url. This class also adds a javascript bridge and a touch listener to the webview and notifies those
 * event to all components that implements the {@link WebViewInterceptorCallback} interface
 */

public class WebViewInterceptor extends WebViewClient {

    public static final String TAG = WebViewInterceptor.class.getSimpleName();
    public static final String WEB_VIEW_JS_BRIDGE = "web_view_bridge";

    // Constants related to placeholders


    private WebViewInterceptorCallback webViewInterceptorCallback;
    private Context context;

    private File htmlTemplatesDir;
    private File cssFile;
    private byte[] cssFileBytes;
    private WeakReference<ViewPager> viewPagerWeakReference; // We need ViewPager reference because WebViewJSBridge's getPagePosition() needs to know the position of the current item in the viewpager

    private GestureDetector webViewGestureDetector;
    private int viewPagerStartingPosition;
    private boolean ignoreFullScreen = false;
    private boolean shouldEnforceCss;
    private ReadingPrefs readingPrefs;

    public WebViewInterceptor(ViewPager viewPager, WebViewInterceptorCallback webViewInterceptorCallback, int viewPagerStartingPosition) {
        this.context = viewPager.getContext();
        viewPagerWeakReference = new WeakReference<ViewPager>(viewPager);
        this.webViewInterceptorCallback = webViewInterceptorCallback;
        this.viewPagerStartingPosition = viewPagerStartingPosition;
        htmlTemplatesDir = new File(new File(context.getFilesDir(), TemplateExtractor.ASSET_EXTRACTION_DESTINATION), TemplateExtractor.ASSET_HTML_FOLDER);
        cssFile = new File(htmlTemplatesDir, TemplateExtractor.ASSET_MAIN_CSS_FILE_NAME);
        webViewGestureDetector = new GestureDetector(context, new WebViewGestureDetector());
        readingPrefs = ReadingPrefs.getInstance(context);

        updateCSS();
    }


    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
        // Check if the image the webview is trying to access actually exists
        // if it doesn't replace it with a place holder image
        Uri requestUri = request.getUrl();
        String requestUrl = request.getUrl().toString();
        String requestExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);

        FeedItemEntity feedItem = ((FeedItemsPagerAdapter.FeedViewPagerObject) webView.getTag()).getFeedItem();
        assert feedItem != null;

        //Log.d(TAG, String.format("shouldInterceptRequest: intercepting url %s", requestUrl));

        if (request.getUrl().toString().equals(Uri.fromFile(cssFile).toString())) {
            //Log.d(TAG, String.format("shouldInterceptRequest: loading css... length is %d", cssFileBytes.length));
            //Log.d(TAG, "shouldInterceptRequest: has css file");
            Log.e(TAG, String.format("shouldInterceptRequest: loading css for %s", feedItem.title));
            return new WebResourceResponse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(request.getUrl().toString())),
                    "UTF-8",
                    new ByteArrayInputStream(cssFileBytes));
        } else if (feedItem != null && feedItem.id.equals(request.getUrl().getLastPathSegment())) {
            // Return the string of the feed item string if it were a file
            //Log.d(TAG, String.format("shouldInterceptRequest: loading feeditem -> %s", feedItem.title));
            byte[] buff = feedItem.hasFullArticle() ? feedItem.fullArticle.getBytes() : feedItem.content.getBytes();

            //Log.d(TAG, String.format("shouldInterceptRequest: %s has full article %b and full article is %s", feedItem.title, feedItem.hasFullArticle(), feedItem.fullArticle));

            //Log.d(TAG, String.format("shouldInterceptRequest: content is %s", feedItem.content));
            return new WebResourceResponse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(PaperApp.HTML_EXT)
                    , PaperApp.UTF8_ENCODING
                    , new ByteArrayInputStream(buff));
        }

        return super.shouldInterceptRequest(webView, request);
    }


    public void setShouldEnforceCss(boolean shouldEnforceCss) {
        this.shouldEnforceCss = shouldEnforceCss;
    }

    @Override
    public void onPageStarted(WebView webView, String url, Bitmap favicon) {
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                webViewGestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });


        super.onPageStarted(webView, url, favicon);
    }

    @Override
    public void onPageFinished(final WebView webView, String url) {
        FeedItemsPagerAdapter.FeedViewPagerObject feedViewPagerObject = (FeedItemsPagerAdapter.FeedViewPagerObject) webView.getTag();
        // Once the page loads remove the progress bar and make the webview visible
        assert feedViewPagerObject != null;
        feedViewPagerObject.getProgressBar().setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        if (webViewInterceptorCallback != null && viewPagerWeakReference.get().getCurrentItem() == viewPagerStartingPosition) {
            webViewInterceptorCallback.viewPagerFistPageLoaded();
            viewPagerStartingPosition = -1;
        }

        // Restore settings to avoid unstyled articles in case of failure
        webView.evaluateJavascript(
                "setBackgroundColor("
                        + "\'" + readingPrefs.backgroundColor + "\'" + ","
                        + "\'" + readingPrefs.textColor + "\'" + ","
                        + "\'" + readingPrefs.linkColor + "\'"
                        + ")"
                , null);

        webView.evaluateJavascript(
                "updateFont('" + readingPrefs.selectedFontName + "')"
                , null);

        webView.evaluateJavascript(
                "updateFontSize("
                        + "'" + readingPrefs.titleFontSize + "',"
                        + "'" + readingPrefs.contentFontSize + "',"
                        + "'" + readingPrefs.articleInfoFontSize + "')"
                , null);


        super.onPageFinished(webView, url);
    }


    public void updateCSS() {
        try {
            cssFileBytes = parseCSS(fileToBytes(cssFile));
        } catch (IOException e) {
            Log.d(TAG, "updateCSS: Error parsing CSS file");
            e.printStackTrace();
        }
    }

    public WebViewJSBridge getWebViewJsBridge() {
        return new WebViewJSBridge();
    }

    // Replace the variable strings in the css file with their actual
    // values read from preferences
    private byte[] parseCSS(byte[] cssBytes) {
        String cssString = new String(cssBytes);
        //Log.d(TAG, String.format("parseCSS: background color is %s", ReadingPrefs.backgroundColor));
        cssString = cssString.replace(CSSConstants.LEAN_BACKGROUND_COLOR, readingPrefs.backgroundColor)
                .replace(CSSConstants.LEAN_TEXT_COLOR, readingPrefs.textColor)
                .replace(CSSConstants.LEAN_LINK_COLOR, readingPrefs.linkColor)
                .replace(CSSConstants.LEAN_ITALIC_FONT, "\"" + readingPrefs.selectedFontName + " Italic\"")
                .replace(CSSConstants.LEAN_BOLD_FONT, "\"" + readingPrefs.selectedFontName + " Bold\"")
                .replace(CSSConstants.LEAN_REGULAR_FONT, "\"" + readingPrefs.selectedFontName + " Regular\"")
                .replace(CSSConstants.LEAN_TITLE_FONT_SIZE, String.valueOf(readingPrefs.titleFontSize) + "px")
                .replace(CSSConstants.LEAN_CONTENT_FONT_SIZE, String.valueOf(readingPrefs.contentFontSize) + "px")
                .replace(CSSConstants.LEAN_ARTICLE_INFO_FONT_SIZE, String.valueOf(readingPrefs.articleInfoFontSize) + "px")
                .replace(CSSConstants.LEAN_LINE_HEIGHT, String.valueOf(readingPrefs.lineHeight))
                .replace(CSSConstants.LEAN_JUSTIFICATION, readingPrefs.justification);
        return cssString.getBytes();
    }

    private byte[] fileToBytes(File file) throws IOException {
        byte[] bytes;
        FileInputStream fileInputStream = new FileInputStream(file);
        bytes = new byte[fileInputStream.available()];
        fileInputStream.read(bytes);
        fileInputStream.close();
        return bytes;
    }


    public interface WebViewInterceptorCallback {
        void showLinkOptionsDialog(String url);

        void openUrl(String url);

        void downloadImage(String domId, String url, String placeholderSrc);

        void openDownloadedImage(String domId);

        void onWebViewSingleTapped();

        void onWebViewDoubleTapped();

        void onWebViewScrolled(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        void viewPagerFistPageLoaded();

        void openYoutubeVideo(String videoId, boolean isList);
    }

    @Keep
    private class WebViewJSBridge {
        @JavascriptInterface
        public void onLinkClicked(String url) {
            if (webViewInterceptorCallback != null) {
                webViewInterceptorCallback.openUrl(url);
            }
        }

        @JavascriptInterface
        public void openYoutubeVideo(String youtubeLink) {
            if (webViewInterceptorCallback != null) {
                // lets the get the video id from the embed link
                Log.e(TAG, String.format("openYoutubeVideo: youtube video link clicked : %s", youtubeLink));

                String[] urlParts = youtubeLink.split("\\?");

                boolean isList = false;

                if (urlParts.length > 0) {
                    String firstPart = urlParts[0];
                    String videoId = Uri.parse(firstPart).getLastPathSegment();

                    if (videoId.equalsIgnoreCase("videoseries")) {
                        HttpUrl url = HttpUrl.parse(youtubeLink);
                        videoId = url.queryParameter("list");
                        isList = true;
                    }

                    if (videoId != null && !videoId.isEmpty()) {
                        Log.e(TAG, String.format("openYoutubeVideo: video id is %s", videoId));
                        webViewInterceptorCallback.openYoutubeVideo(videoId, isList);
                    }

                }


            }

        }


        @JavascriptInterface
        public void onLinkLongClicked(String url) {
            if (webViewInterceptorCallback != null) {
                webViewInterceptorCallback.showLinkOptionsDialog(url);
            }
        }

        @JavascriptInterface
        public void downloadImage(String id, String url, String placeHolderSrc) {
            Log.d(TAG, "downloadImage: downloading image");
            if (webViewInterceptorCallback != null) {
                webViewInterceptorCallback.downloadImage(id, url, placeHolderSrc);
            }
        }

        @JavascriptInterface
        public void showDownloadedImage(String domId) {
            Log.d(TAG, "showDownloadedImage: showing downloaded image");
            if (webViewInterceptorCallback != null) {
                webViewInterceptorCallback.openDownloadedImage(domId);
            }
        }

        @JavascriptInterface
        public int getPagePosition() {
            return viewPagerWeakReference.get().getCurrentItem();
        }

        @JavascriptInterface
        public String getRelativeTime(String timestamp) {

            String timeInfo;
            long feedTime = Long.valueOf(timestamp);

            if (DateUtils.isToday(feedTime)) {
                timeInfo = context.getString(R.string.today) + " " + context.getString(R.string.at) + " " + DateUtils.formatDateTime(context, feedTime, android.text.format.DateUtils.FORMAT_SHOW_TIME);
            } else {

                timeInfo = DateUtils.formatDateTime(context,
                        com.biniam.rss.utils.Utils.getMidNightTimeStamp(feedTime),
                        DateUtils.FORMAT_SHOW_DATE | android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_NO_YEAR
                ) + " " + context.getString(R.string.at) + " " + DateUtils.formatDateTime(context, feedTime, android.text.format.DateUtils.FORMAT_SHOW_TIME);
            }

            return timeInfo;
        }

        @JavascriptInterface
        public String getArticleDownloadingString() {
            return context.getString(R.string.article_downloading);
        }

        @JavascriptInterface
        public String getArticleDownloadErrorString() {
            return context.getString(R.string.article_download_error);
        }

        @JavascriptInterface
        public String getDoubleTapToRetryString() {
            return context.getString(R.string.double_tap_to_retry);
        }

        @JavascriptInterface
        public String getShowOriginalString() {
            return context.getString(R.string.show_original).toUpperCase();
        }

        @JavascriptInterface
        public String getRetryString() {
            return context.getString(R.string.retry).toUpperCase();
        }

        @JavascriptInterface
        public void setIgnoreFullScreen(boolean ignore) {
            Log.d(TAG, String.format("setIgnoreFullScreen: ignore full screen -> %b", ignore));
            ignoreFullScreen = ignore;
        }

        @JavascriptInterface
        public void showFullArticle() {

        }
    }

    private class WebViewGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (webViewInterceptorCallback != null) {
                webViewInterceptorCallback.onWebViewDoubleTapped();
            }
            return super.onDoubleTap(e);
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (webViewInterceptorCallback != null) {
                webViewInterceptorCallback.onWebViewScrolled(e1, e2, distanceX, distanceY);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (webViewInterceptorCallback != null && !ignoreFullScreen) {
                webViewInterceptorCallback.onWebViewSingleTapped();
            }
            return super.onSingleTapConfirmed(e);
        }
    }
}
