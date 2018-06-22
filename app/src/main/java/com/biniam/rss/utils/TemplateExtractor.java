package com.biniam.rss.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.biniam.rss.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by  M on 5/4/17.
 * <p>
 * Extracts html resources, images and fonts from the assets directory to the app data directory;
 */

public class TemplateExtractor {

    public static final String TAG = TemplateExtractor.class.getSimpleName();

    // Folder names in the assets
    public static final String ASSET_JQUERY_FILE_NAME = "jquery.js";
    public static final String ASSET_MAIN_JS_FILE_NAME = "main.js";
    public static final String ASSET_MAIN_CSS_FILE_NAME = "main.css";
    public static final String ASSET_PLAY_VIDEO_ICON_FILE_NAME = "play_video.svg";
    public static final String ASSET_GENERIC_CONTENT_ICON_FILE_NAME = "launch_web_content.svg";
    public static final String ASSET_PRETTIFY_JS = "run_prettify.js";
    public static final String ASSET_PRETTIFY_CSS = "prettify.css";

    public static final String ASSET_HTML_FOLDER = "html/";
    public static final String ASSET_EXTRACTION_DESTINATION = "templates/";


    private Context context;
    private static TemplateExtractionHelperCallback templateExtractionHelperCallback;

    public TemplateExtractor(Context context) {
        this.context = context;
    }


    public void startExtraction() {
        // Start extracting the fonts folder to #{ASSET_EXTRACTION_DESTINATION}
        new TemplateExtractorAsyncTask(context.getAssets(), context.getFilesDir()).execute(
                context.getResources().getStringArray(R.array.font_paths), new String[]{"html"}
        );

    }

    public void setTemplateExtractionHelperCallback(TemplateExtractionHelperCallback templateExtractionHelperCallback) {
        TemplateExtractor.templateExtractionHelperCallback = templateExtractionHelperCallback;
    }


    private static class TemplateExtractorAsyncTask extends AsyncTask<String[], Void, Boolean> {

        private AssetManager assetManager;
        private File filesDir;

        TemplateExtractorAsyncTask(AssetManager assetManager, File filesDir) {
            this.assetManager = assetManager;
            this.filesDir = filesDir;
        }


        @Override
        protected Boolean doInBackground(String[]... strings) {

            for (String[] paths : strings) {
                Log.d(TAG, String.format("doInBackground: path count is %d", paths.length));

                File dirPath = new File(filesDir, ASSET_EXTRACTION_DESTINATION);
                dirPath.mkdirs();


                for (String path : paths) {
                    try {
                        String[] files = assetManager.list(path);
                        for (String file : files) {
                            File subDir = new File(dirPath, path);
                            subDir.mkdirs();

                            InputStream is = assetManager.open(path + "/" + file);
                            OutputStream fileOutputStream = new FileOutputStream(new File(subDir, file));
                            byte[] data = new byte[is.available()];
                            is.read(data);
                            fileOutputStream.write(data);
                            is.close();
                            fileOutputStream.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            if (templateExtractionHelperCallback != null)
                templateExtractionHelperCallback.onTemplateExtractionStarted();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (templateExtractionHelperCallback != null) {
                if (success) {
                    templateExtractionHelperCallback.onTemplateExtractionFinished();
                } else {
                    templateExtractionHelperCallback.onTemplateExtractionError();
                }
            }
            super.onPostExecute(success);
        }
    }

    public interface TemplateExtractionHelperCallback {
        default void onTemplateExtractionStarted() {
        }

        default void onTemplateExtractionError() {
        }

        default void onTemplateExtractionFinished() {
        }
    }
}
