package com.biniam.rss.connectivity.inoreader.inoReaderApi;

import android.support.annotation.Keep;

import com.biniam.rss.models.inoreader.InoFoldersTagsList;
import com.biniam.rss.models.inoreader.InoIteamId;
import com.biniam.rss.models.inoreader.InoReaderSubscriptionItems;
import com.biniam.rss.models.inoreader.InoReaderUserInfo;
import com.biniam.rss.models.inoreader.InoStreamContentList;
import com.biniam.rss.models.inoreader.InoSubscriptionList;
import com.biniam.rss.models.inoreader.InoUnreadCount;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by biniam on 2/15/18.
 */
@Keep
public interface InoReaderAPI {
    @GET("/reader/api/0/user-info")
    Observable<InoReaderUserInfo> userInfo();

    @GET("/reader/api/0/subscription/list")
// will change the get method to Post to see the Difference
    Observable<InoSubscriptionList> getSubscriptionList();

    @POST("/reader/api/0/subscription/quickadd")
    Observable<InoReaderSubscriptionItems> addNewSubscription(@QueryMap Map<String, String> options);

    @POST("/reader/api/0/subscription/edit")
    Observable<ResponseBody> editInoSubscription(@QueryMap Map<String, String> options);

    @GET("/reader/api/0/tag/list")
    Observable<InoFoldersTagsList> getTagList();

    @GET("/reader/api/0/unread-count")
    Observable<InoUnreadCount> getInoUnreadCount();

    @POST("/reader/api/0/stream/contents/{streamId}")
    Observable<InoStreamContentList> getFeedItems(@Path("streamId") String streamId);

    @POST("/reader/api/0/stream/contents/{streamId}")
    Observable<InoStreamContentList> getStreamContent(@Path("streamId") String streamId, @QueryMap Map<String, String> options);

    @POST("/reader/api/0/stream/contents/")
    Observable<InoStreamContentList> getStreamContent(@QueryMap Map<String, String> options);

    @POST("/reader/api/0/stream/contents/{streamId}")
    Observable<InoStreamContentList> syncStreamContentBySub(@Path("streamId") String streamId);

    @POST("/reader/api/0/stream/contents")
    Observable<InoStreamContentList> getStarred(@QueryMap Map<String, String> starred);

    @POST("/reader/api/0/rename-tag")
    Observable<Boolean> renameTag(@QueryMap Map<String, String> renameTagOptions);

    @POST("/reader/api/0/disable-tag")
    Observable<Boolean> deleteTag(@QueryMap Map<String, String> deleteTagOption);

    @POST("/reader/api/0/mark-all-as-read")
    Observable<ResponseBody> markAllAsRead(@QueryMap Map<String, String> options);

    @POST("/reader/api/0/edit-tag")
    Observable<ResponseBody> editFeedItems(@Body String string);

    @POST("/reader/api/0/edit-tag")
    Observable<ResponseBody> editTag(@QueryMap Map<String, String> options);


}
