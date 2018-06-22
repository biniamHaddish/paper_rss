package com.biniisu.leanrss.ui.controllers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.URLUtil;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.utils.TemplateExtractor;
import com.biniisu.leanrss.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

import okhttp3.HttpUrl;

import static com.biniisu.leanrss.utils.CSSConstants.LEAN_ARTICLE_BODY;
import static com.biniisu.leanrss.utils.CSSConstants.LEAN_ARTICLE_TITLE;
import static com.biniisu.leanrss.utils.CSSConstants.LEAN_JQUERY_PATH;
import static com.biniisu.leanrss.utils.CSSConstants.LEAN_MAIN_CSS_PATH;
import static com.biniisu.leanrss.utils.CSSConstants.LEAN_MAIN_JS_PATH;
import static com.biniisu.leanrss.utils.CSSConstants.LEAN_PRETTIFY_CSS;
import static com.biniisu.leanrss.utils.CSSConstants.LEAN_PRETTIFY_JS;
import static com.biniisu.leanrss.utils.CSSConstants.LEAN_SOURCE_INFO;

/**
 * Created by biniam_Haddish on 5/12/17.
 *
 * This classes pre-processes the article html to suitable form desired
 */

public class FeedParser {

    public static final String TAG = FeedParser.class.getSimpleName();
    public static final String EMBEDLY_HOST = "cdn.embedly.com";

    public static final String IMG_TAG = "img";
    public static final String STYLE_ATTR = "style";
    public static final String IFRAME_TAG = "iframe";
    public static final String SOURCE_TAG = "source";
    public static final String PICTURE_TAG = "picture";
    public static final String HTML_TAG = "html";
    public static final String BODY_TAG = "body";
    public static final String HEAD_TAG = "head";
    public static final String TITLE_TAG = "title";
    public static final String DIV_TAG = "div";


    // Code tags `<pre>`, `<code>`, or `<xmp>`
    public static final String PRE_TAG = "pre";
    public static final String CODE_TAG = "code";
    public static final String XMP_TAG = "xmp";
    public static final String PRETTIFY_CLASS = "prettyprint";


    // Constants for HTML attributes
    public static final String SRC_ATTR = "src";
    public static final String HREF_ATTR = "href";
    public static final String SVG_TAG = "svg";
    public static final String P_TAG = "p";
    public static final String ORG_SRC_ATTR = "org_src";
    public static final String ID_ATTR = "id";
    public static final String WIDTH_ATTR = "width";
    public static final String HEIGHT_ATTR = "height";
    public static final String SRCSET_ATTR = "srcset";
    public static final String ALT_ATTR = "alt";
    public static final String ORG_ALT_ATTR = "org_alt";
    public static final String MEDIA_ATTR = "media";
    public static final String SMALL_MEDIA_ATTR_VALUE = "--small";
    public static final String X_LARGE_MEDIA_ATTR_VALUE = "--xlarge";
    public static final String CLASS_ATTR = "class";
    public static final String DATA_SRCSET_ATTR = "data-srcset";
    public static final String IMG_DOWNLAODED = "img-downloaded";
    public static final String TIME_INFO_ELEMENT_ID = "timeinfo";
    public static final String TIMESTAMP_ATTRIBUTE = "timestamp";
    public static final String IFRAME_SRC_ATTR = "iframe_src";
    public static final String PLACEHOLDER_CLASS = "img-placeholder";
    public static final String IMAGE_CLASS = "image";
    public static final String YOUTUBE_CLASS = "utube";
    public static final String GENERIC_CONTENT_CLASS = "generic";
    public static final String EXTERNAL_CONTENT_CLASS = "ext_content";
    public static final String EXT_YOUTUBE_ICON_CLASS = "ext_youtube_icon";
    public static final String EXT_GENERIC_ICON_CLASS = "ext_generic_icon";
    public static final String YOUTUBE = "youtube";
    public static final String MAIN_DIV = "main";


    public static final String ARTICLE_HTML = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "\t<script type=\"text/javascript\" src=\"{LEAN.JQUERY_PATH}\"></script>\n" +
            "\t<script type=\"text/javascript\" src=\"{LEAN.PRETTIFY_JS_PATH}\"></script>\n" +
            "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"{LEAN.PRETTIFY_CSS_PATH}\">\n" +
            "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"{LEAN.MAIN_CSS_PATH}\">\n" +
            "\t<script type=\"text/javascript\" src=\"{LEAN.MAIN_JS_PATH}\"></script>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <p id=\"title\">{LEAN.ARTICLE_TITLE}</p>\n" +
            "    <p id=\"sourceinfo\">{LEAN.SOURCE_INFO}</p>" +
            "    <p id=\"timeinfo\">{LEAN.TIME_INFO}</p>" +
            "    <div id=\"main\">{LEAN.ARTICLE_BODY}</div>\n" +
            "</body>\n" +
            "</html>";

    public static final int MAX_TEXT_LENGTH_FOR_EXCERPT = 120;
    public static final int MAX_WORD_COUNT_FOR_EXCERPT = 15;
    private static FeedParser feedParser;
    private File templatesDir;
    private File htmlDir;
    private File playIcon;
    private File genericContentIcon;
    private File htmlTemplatesDir;
    private File jqueryFile;
    private File mainJsFile;
    private File prettifyJsFile;
    private File mainCssFile;
    private File prettifyCssFile;
    private Context context;

    private FeedParser(Context context) {
        this.context = context;

        templatesDir = new File(context.getFilesDir(), TemplateExtractor.ASSET_EXTRACTION_DESTINATION);
        htmlTemplatesDir = new File(templatesDir, TemplateExtractor.ASSET_HTML_FOLDER);

        htmlDir = new File(templatesDir, TemplateExtractor.ASSET_HTML_FOLDER);
        playIcon = new File(htmlDir, TemplateExtractor.ASSET_PLAY_VIDEO_ICON_FILE_NAME);
        genericContentIcon = new File(htmlDir, TemplateExtractor.ASSET_GENERIC_CONTENT_ICON_FILE_NAME);
        jqueryFile = new File(htmlTemplatesDir, TemplateExtractor.ASSET_JQUERY_FILE_NAME);
        mainJsFile = new File(htmlTemplatesDir, TemplateExtractor.ASSET_MAIN_JS_FILE_NAME);
        prettifyJsFile = new File(htmlTemplatesDir, TemplateExtractor.ASSET_PRETTIFY_JS);
        mainCssFile = new File(htmlTemplatesDir, TemplateExtractor.ASSET_MAIN_CSS_FILE_NAME);
        prettifyCssFile = new File(htmlTemplatesDir, TemplateExtractor.ASSET_PRETTIFY_CSS);
    }

    public static FeedParser getInstance(Context context) {
        if (feedParser == null) feedParser = new FeedParser(context);
        return feedParser;
    }

    public FeedItemEntity parseFeedItem(FeedItemEntity feedItem) {

        String sourceInfo;

        if (feedItem.author != null && !feedItem.author.isEmpty()) {
            sourceInfo = context.getString(R.string.by) + " <strong>" + feedItem.author + "</strong>" + ", " + Uri.parse(feedItem.link).getHost();
        } else {
            sourceInfo = Uri.parse(feedItem.link).getHost();
        }

        if (feedItem.fullArticle != null && !feedItem.fullArticle.isEmpty()) {

            feedItem.fullArticle =
                    ARTICLE_HTML
                            .replace(LEAN_JQUERY_PATH, Uri.fromFile(jqueryFile).toString())
                            .replace(LEAN_MAIN_JS_PATH, Uri.fromFile(mainJsFile).toString())
                            .replace(LEAN_MAIN_CSS_PATH, Uri.fromFile(mainCssFile).toString())
                            .replace(LEAN_PRETTIFY_CSS, Uri.fromFile(prettifyCssFile).toString())
                            .replace(LEAN_PRETTIFY_JS, Uri.fromFile(prettifyJsFile).toString())
                            .replace(LEAN_ARTICLE_TITLE, feedItem.title)
                            .replace(LEAN_ARTICLE_BODY, feedItem.fullArticle)
                            .replace(LEAN_SOURCE_INFO, sourceInfo);

            Document fullArticleDOM = Jsoup.parse(feedItem.fullArticle);

            // Add a timestamp attribute to '#timeinfo' so that time becomes relative with the current time
            fullArticleDOM.getElementById(TIME_INFO_ELEMENT_ID).attr(TIMESTAMP_ATTRIBUTE, String.valueOf(feedItem.published));
            fullArticleDOM = cleanUpDOM(fullArticleDOM, context);
            fullArticleDOM = parseImages(fullArticleDOM, context);


            feedItem.fullArticle = fullArticleDOM.toString();
        }

        feedItem.content = ARTICLE_HTML
                .replace(LEAN_JQUERY_PATH, Uri.fromFile(jqueryFile).toString())
                .replace(LEAN_MAIN_JS_PATH, Uri.fromFile(mainJsFile).toString())
                .replace(LEAN_MAIN_CSS_PATH, Uri.fromFile(mainCssFile).toString())
                .replace(LEAN_PRETTIFY_CSS, Uri.fromFile(prettifyCssFile).toString())
                .replace(LEAN_PRETTIFY_JS, Uri.fromFile(prettifyJsFile).toString())
                .replace(LEAN_ARTICLE_TITLE, feedItem.title)
                .replace(LEAN_ARTICLE_BODY, feedItem.content)
                .replace(LEAN_SOURCE_INFO, sourceInfo);

        Document descriptionDOM = Jsoup.parse(feedItem.content);

        // Add a timestamp attribute to '#timeinfo' so that time becomes relative with the current time
        descriptionDOM.getElementById(TIME_INFO_ELEMENT_ID).attr(TIMESTAMP_ATTRIBUTE, String.valueOf(feedItem.published));
        descriptionDOM = cleanUpDOM(descriptionDOM, context);
        descriptionDOM = parseImages(descriptionDOM, context);
        feedItem.content = descriptionDOM.toString();

        if (feedItem.excerpt == null) feedItem.excerpt = makeExcerpt(descriptionDOM);

        Log.d(TAG, String.format("parseFeedItem: content is %s", feedItem.content));

        return feedItem;
    }

    private Document cleanUpDOM(Document document, Context context) {

        Elements elements = document.getAllElements();

        // Remove all classes and styles
        elements.removeAttr(CLASS_ATTR);
        elements.removeAttr(STYLE_ATTR);


        Elements iframes = document.getElementsByTag(IFRAME_TAG);

        for (Element iframe : iframes) {
            String iframeSrc = iframe.attr(SRC_ATTR);

            HttpUrl url = HttpUrl.parse(iframeSrc);

            if (url == null) {
                iframe.remove();
                continue;
            }

            Log.d(TAG, String.format("cleanUpDOM: url host is %s", url.host()));

            if (url.host().equals(EMBEDLY_HOST)) {
                if (url.queryParameter(SRC_ATTR) != null) {
                    iframeSrc = url.queryParameter(SRC_ATTR);
                }
            }


            Log.d(TAG, String.format("cleanUpDOM: found iframe with src %s", iframeSrc));

            if (!URLUtil.isValidUrl(iframeSrc)) {
                // We have invalid url for the iframe we remove the iframe and continue to the next one
                iframe.remove();
                continue;
            }

            iframe.text("");

            iframe.removeAttr(SRC_ATTR);
            iframe.removeAttr(WIDTH_ATTR);
            iframe.removeAttr("frameborder");
            iframe.removeAttr("allowfullscren");


            Element divEl = iframe.tagName(DIV_TAG);
            divEl.addClass(EXTERNAL_CONTENT_CLASS);

            Elements iframeChildren = divEl.children();
            iframeChildren.remove();




            if (iframeSrc.contains(YOUTUBE)) {

                divEl.addClass(YOUTUBE_CLASS);
                divEl.append(
                        "<img class=\"" + EXT_YOUTUBE_ICON_CLASS + "\" src=\"" + Uri.fromFile(playIcon) + "\" />" +
                                "<p>" + context.getString(R.string.open_youtube_content) + "</p>"
                );

                Log.e(TAG, String.format("cleanUpDOM: youtube element is %s", divEl.toString()));
            } else {
                divEl.addClass(GENERIC_CONTENT_CLASS);
                divEl.append(
                        "<img class=\"" + EXT_GENERIC_ICON_CLASS + "\" src=\"" + Uri.fromFile(genericContentIcon) + "\"  />" +
                                "<p>" + context.getString(R.string.open_generic_content) + "</p>"
                );
            }

            divEl.attr(IFRAME_SRC_ATTR, iframeSrc);
            //iframe.remove();
        }

        // Add prettyprint class to code tags
        Elements preTags = document.getElementsByTag(PRE_TAG);
        Elements codeTag = document.getElementsByTag(CODE_TAG);
        Elements xmpTag = document.getElementsByTag(XMP_TAG);

        Elements codeTags = new Elements();
        codeTags.addAll(preTags);
        codeTags.addAll(codeTag);
        codeTags.addAll(xmpTag);

        for (Element tag :
                codeTags) {
            tag.addClass(PRETTIFY_CLASS);
        }

        return document;
    }


    /**
     *
     * @param document
     * @param context
     * @return
     */
     @NonNull
     private Document parseImages(Document document, Context context) {

         Elements imgTags = document.getElementsByTag(IMG_TAG);

        for (Element imgTag : imgTags) {
            String src = imgTag.attr(SRC_ATTR);

            // Dont' process iframe replacement icons
            if (imgTag.className().equals(EXT_YOUTUBE_ICON_CLASS) || imgTag.className().equals(EXT_GENERIC_ICON_CLASS)) {
                continue;
            }


            if (imgTag.hasAttr(HREF_ATTR) && (src == null || src.isEmpty()))
                src = imgTag.attr(HREF_ATTR);

            if (imgTag.parent().tagName().toLowerCase().equals(PICTURE_TAG)) {
                imgTag.parent().attr(CLASS_ATTR, IMAGE_CLASS);
                imgTag.attr(CLASS_ATTR, PLACEHOLDER_CLASS);
            } else if (imgTag.hasAttr(SRCSET_ATTR)) {
                String[] srcs = imgTag.attr(SRCSET_ATTR).split(",");

                if (srcs.length > 0) {
                    imgTag.removeAttr(SRCSET_ATTR);
                    src = getBestImage(srcs, context);
                }

                imgTag.attr(CLASS_ATTR, IMAGE_CLASS + " " + PLACEHOLDER_CLASS);

            } else {
                imgTag.attr(CLASS_ATTR, IMAGE_CLASS + " " + PLACEHOLDER_CLASS);
            }

            if (src != null) {
                imgTag.attr(ID_ATTR, Utils.getSHA1Digest(src).substring(0, 4)); // Give the img tag a unique id used to refer it while loading images
                imgTag.attr(ORG_SRC_ATTR, src);
            }


            // Remove alt attributes from images they cause an ugly image place holder to load
            if (imgTag.hasAttr(ALT_ATTR)) {
                String altString = imgTag.attr(ALT_ATTR);
                if (altString != null && !altString.isEmpty()) imgTag.attr(ORG_ALT_ATTR, altString);
                imgTag.removeAttr(ALT_ATTR);
            }

            imgTag.removeAttr(HREF_ATTR); // To avoid loading of the image
            imgTag.removeAttr(SRCSET_ATTR);
            imgTag.removeAttr(SRC_ATTR);
            imgTag.attr(IMG_DOWNLAODED, "false");
        }

        Elements sourceTags = document.getElementsByTag(SOURCE_TAG);
        if (sourceTags != null) sourceTags.remove();

        return document;
    }


    private String getBestImage(String[] srcs, Context context) {

        String widthIdentifier = "w";
        String pixelDensityIdentifier = "x";

        DisplayMetrics displayMetrics = Utils.getDeviceMetrics(context);

        float deviceWidth = displayMetrics.widthPixels / displayMetrics.density;
        float deviceDensity = context.getResources().getDisplayMetrics().density;

        Log.d(TAG, String.format("getBestImage: caclulated deviceWidth is %f", deviceWidth));

        float smallestDifference = -1;
        String bestResSrc = null;
        String baseSrc = null;

        boolean isDensityProp = false;

        for (String src : srcs) {
            if (src.contains(" ")) {
                src = src.trim();
                String url = src.split(" ")[0];

                if (src.split(" ").length < 2) {
                    continue;
                }

                String imagePropString = src.split(" ")[1];

                Log.d(TAG, String.format("getBestImage: imagePropString -> %s", imagePropString));

                if (imagePropString.toLowerCase().contains(widthIdentifier)) {
                    imagePropString = imagePropString.replace(widthIdentifier, "");
                } else if (imagePropString.toLowerCase().contains(pixelDensityIdentifier)) {
                    imagePropString = imagePropString.replace(pixelDensityIdentifier, "");
                    isDensityProp = true;
                }

                Float imageProp = Float.valueOf(imagePropString);

                Log.d(TAG, String.format("getBestImage: calculating width %f", imageProp));

                Float srcDiff = (imageProp - (isDensityProp ? deviceDensity : deviceWidth));

                if (srcDiff > smallestDifference) {
                    smallestDifference = srcDiff;
                    bestResSrc = url;
                }

                Log.d(TAG, String.format("getBestImage: the best width is now %f", smallestDifference));
            } else {
                baseSrc = src;
            }
        }

        if (smallestDifference == -1) {
            Log.d(TAG, String.format("getBestImage: returning base src %s", baseSrc));
            return baseSrc;
        }

        return bestResSrc;
    }


    // Tries to make a summary text from a given html document
    private String makeExcerpt(Document dom) {

        if (dom != null && !dom.getAllElements().isEmpty()) {
            Elements bodyElements = dom.getElementById(MAIN_DIV).getAllElements();
            StringBuilder failSafeExcerpt = new StringBuilder();
            for (Element element : bodyElements) {
                if (!element.hasText()) continue;
                failSafeExcerpt.append(element.text());
            }

            String[] words = failSafeExcerpt.toString().split("\\s+");
            StringBuilder finalExcerpt = new StringBuilder();

            if (words.length <= MAX_WORD_COUNT_FOR_EXCERPT) return failSafeExcerpt.toString();

            for (int i = 0; i < MAX_WORD_COUNT_FOR_EXCERPT; i++) {
                finalExcerpt.append(words[i]);
                finalExcerpt.append(" ");
            }

            return finalExcerpt.toString();
        }

        return null;
    }
}
