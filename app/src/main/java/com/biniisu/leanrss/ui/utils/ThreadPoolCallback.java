package com.biniisu.leanrss.ui.utils;

import android.os.Message;

/**
 * Created by biniam_Haddish Teweldeberhan on 7/14/17.
 * <p>
 * A simple interface for communication between background threads and the feed reader UI
 */

public interface ThreadPoolCallback {
    void publish(Message message);
}
