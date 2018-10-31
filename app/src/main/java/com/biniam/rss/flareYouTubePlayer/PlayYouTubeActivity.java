package com.biniam.rss.flareYouTubePlayer;

import android.os.Bundle;

import com.biniam.rss.R;
import com.biniam.rss.utils.Constants;
import com.biniam.rss.utils.PaperApp;
import com.biniam.rss.utils.Utils;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.List;

/**
 * Created by biniam on 1/10/18.
 * <p>
 * This Activity Will play YouTube video Directly inside Readably App
 * {@link com.biniam.rss.flareYouTubePlayer}
 */

public class PlayYouTubeActivity extends YouTubeBaseActivity {

    public static final String VIDEO_ID_EXTRA = "VIDEO_ID_EXTRA";
    public static final String LIST_ID_EXTRA = "LIST_ID_EXTRA";
    private static YouTubePlayerView youTubePlayerView;

    /**
     * responsible for playing Video.
     *
     * @param id
     */
    public static void playTubeVideo(String id, boolean isList) {
        if (id != null || !id.isEmpty()) {
            youTubePlayerView.initialize(Constants.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    youTubePlayer.cueVideo("5xVh-7ywKpE");

                    if (isList) {
                        youTubePlayer.loadPlaylist(id);
                    } else {
                        youTubePlayer.loadVideo(id);
                    }

                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    Utils.showToast(PaperApp.getInstance(), "Can not play the video.");
                }
            });
        }
    }

    /**
     * Will play list of Videos
     *
     * @param videoIdList
     */
    public static void playVideoFromList(List<String> videoIdList) {
        if (videoIdList != null) {
            youTubePlayerView.initialize(Constants.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    // play video from list
                    youTubePlayer.loadVideos(videoIdList);
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    Utils.showToast(PaperApp.getInstance(), "Can not play the video.");
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_paly_you_tube);
        youTubePlayerView = findViewById(R.id.youTubePlayer);

        if (getIntent() != null && getIntent().hasExtra(VIDEO_ID_EXTRA)) {
            String videoId = getIntent().getStringExtra(VIDEO_ID_EXTRA);
            if (videoId != null && !videoId.isEmpty()) {
                playTubeVideo(videoId, false);
            }
        }

        if (getIntent() != null && getIntent().hasExtra(LIST_ID_EXTRA)) {
            String listId = getIntent().getStringExtra(LIST_ID_EXTRA);
            if (listId != null && !listId.isEmpty()) {
                playTubeVideo(listId, true);
            }
        }

    }
}
