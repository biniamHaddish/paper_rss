package com.biniisu.leanrss.utils;


import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.URLUtil;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam_Haddish on 1/18/18.
 * <p>
 * Get favicon for the website
 */

public class FavIconFetcher {

    public static final String TAG = FavIconFetcher.class.getSimpleName();


    // ICON TYPES
    public static final String ICON = "icon";
    public static final String SHORTCUT_ICON = "shortcut icon";
    public static final String APPLE_TOUCH_ICON = "apple-touch-icon";
    public static final String APPLE_TOUCH_ICON_PRECOMPOSED = "apple-touch-icon-precomposed";
    public static final String MANIFEST_REL = "manifest";
    public static final String MANIFEST_ICON_PROP = "icons";

    public static final String LINK_TAG = "link";
    public static final String REL_ATTR = "rel";
    public static final String ABS_HREF = "abs:href";

    private static OkHttpClient client = new OkHttpClient();


    public static String getFavIconUrl(String webSiteUrl) {

        try {

            Log.e(TAG, String.format("getting favicon now for %s", Uri.parse(webSiteUrl)));


            Document pageDom = Jsoup.parse(new URL(new URL(webSiteUrl), ""), 20000);


            Elements links = pageDom.getElementsByTag(LINK_TAG);


            // Check for manifest json
            for (Element link : links) {
                if (link.attr(REL_ATTR).equals(MANIFEST_REL)) {
                    Gson gson = new Gson();
                    Log.w(TAG, String.format("getFavIconUrl: found manifest at %s", link.attr(ABS_HREF)));
                    String json = getUrlStringContents(link.attr(ABS_HREF));


                    Manifest manifest = gson.fromJson(json, Manifest.class);

                    if (manifest != null && manifest.icons != null && !manifest.icons.isEmpty()) {
                        int largestIcon = 0;
                        String largestIconPath = "";

                        for (ManifestIcon manifestIcon : manifest.icons) {

                            String sizeProperty = null;

                            if (manifestIcon.sizes != null && !manifestIcon.sizes.isEmpty()) {
                                sizeProperty = manifestIcon.sizes;
                            } else if (manifestIcon.size != null && !manifestIcon.size.isEmpty()) {
                                sizeProperty = manifestIcon.size;
                            }

                            if (sizeProperty == null) continue;

                            if (sizeProperty.split("x").length > 0) {
                                int size = Integer.valueOf(sizeProperty.split("x")[0]);
                                if (size > largestIcon) {
                                    largestIconPath = cleanUpFavIconUrl("https://" + Uri.parse(link.attr(ABS_HREF)).getHost(), manifestIcon.src);
                                }

                                Log.d(TAG, String.format("getFavIconUrl: largest icon yet is %d", largestIcon));
                                largestIcon = size;
                            }
                        }


                        Log.e(TAG, String.format("getFavIconUrl: manifest icon path is %s", largestIconPath));

                        if (URLUtil.isValidUrl(largestIconPath)) return largestIconPath;
                    }
                }
            }


            // Check for apple touch precomposed icons
            for (Element link : links) {
                if (link.attr(REL_ATTR).equalsIgnoreCase(APPLE_TOUCH_ICON_PRECOMPOSED)) {
                    Log.d(TAG, String.format("getFavIconUrl: %s icon link is %s", APPLE_TOUCH_ICON_PRECOMPOSED, link.attr(ABS_HREF)));
                    return link.attr(ABS_HREF);
                }
            }


            // Check for apple touch icons
            for (Element link : links) {
                if (link.attr(REL_ATTR).equalsIgnoreCase(APPLE_TOUCH_ICON)) {
                    Log.d(TAG, String.format("getFavIconUrl: %s icon link is %s", APPLE_TOUCH_ICON, link.attr(ABS_HREF)));
                    return link.attr(ABS_HREF);
                }
            }

            // Check for shortcut icon
            for (Element link : links) {
                if (link.attr(REL_ATTR).equalsIgnoreCase(SHORTCUT_ICON)) {
                    Log.d(TAG, String.format("getFavIconUrl: %s icon link is %s", SHORTCUT_ICON, link.attr(ABS_HREF)));
                    return link.attr(ABS_HREF);
                }
            }

            //  Check for icon
            for (Element link : links) {
                if (link.attr(REL_ATTR).equalsIgnoreCase(ICON)) {
                    Log.d(TAG, String.format("getFavIconUrl: %s icon link is %s", ICON, link.attr(ABS_HREF)));
                    return link.attr(ABS_HREF);
                }
            }


        } catch (Exception e) {
            Log.e("FavIconFetcher", String.format("getFavIconUrl: error getting favicon for %s, reason : %s ", webSiteUrl, e.getMessage()));
            return "";
        }


        return "";
    }


    public static String getUrlStringContents(String pageUrl) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (pageUrl == null || pageUrl.isEmpty() || !URLUtil.isValidUrl(pageUrl)) {
                return null;
            }
        }

        Log.d("FavIconFetcher", String.format("getUrlStringContents: getting string for %s", pageUrl));

        client.followRedirects();
        Request request = new Request.Builder().url(pageUrl).build();
        Response response = client.newCall(request).execute();
        String body = response.body().string();
        response.close();
        return body;
    }

    private static String cleanUpFavIconUrl(String parentURl, String url) {

        String favIconLink = url;


        if (!URLUtil.isValidUrl(url)) {

            String parentUrlLastChar = String.valueOf(parentURl.charAt(parentURl.length() - 1));
            String urlFirstChar = String.valueOf(url.charAt(0));

            if (parentUrlLastChar.equals("/") || urlFirstChar.equals("/")) {
                favIconLink = parentURl + favIconLink;
            } else {
                favIconLink = parentURl + "/" + favIconLink;
            }

            return favIconLink;
        }

        return url;
    }

    public class Manifest {
        List<ManifestIcon> icons;
    }

    public class ManifestIcon {
        String src;
        String sizes;
        String size;
    }


}