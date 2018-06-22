package com.biniam.rss.connectivity.feedWangler;

import java.util.List;

/**
 * Created by biniam on 12/29/17.
 */

public class FeedWranglerUtils {

    /**
     * will Build comma separated String ids
     *
     * @param ids
     * @return
     */
    public static Object commaSeparatedStringFeedIds(List<String> ids) {
        StringBuilder commaSepValueBuilder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            commaSepValueBuilder.append(ids.get(i));
            //if the value is not the last element of the list then append the comma(,)
            if (i != ids.size() - 1) {
                commaSepValueBuilder.append(",");
            }
        }
        return commaSepValueBuilder.toString();
    }

}
