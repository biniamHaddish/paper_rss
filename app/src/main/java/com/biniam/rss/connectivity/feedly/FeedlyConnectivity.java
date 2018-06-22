package com.biniam.rss.connectivity.feedly;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.biniam.rss.models.feedly.FeedlyCatagories;
import com.biniam.rss.models.feedly.FeedlyDynamicEntryList;
import com.biniam.rss.models.feedly.FeedlyEntries;
import com.biniam.rss.models.feedly.FeedlyEntryIdsListOfStream;
import com.biniam.rss.models.feedly.FeedlyFeedItems;
import com.biniam.rss.models.feedly.FeedlySearchResultItem;
import com.biniam.rss.models.feedly.FeedlySubscription;
import com.biniam.rss.models.feedly.FeedlyUnreadCount;
import com.biniam.rss.models.feedly.FeedlyUserProfile;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by biniam on 3/4/17.
 */

public class FeedlyConnectivity {

    // TODO: 3/4/17 this Class will Deal with the Feedly  connectivity APi.


    public static final String FEEDLY_AUTH_PREF = "FEEDLY_AUTH_PREF";
    public static final HttpUrl Feedly_Search_Parameter =
            HttpUrl.parse(FeedlyConstants.FEEDLY_FEED_SEARCH_URL);
    public SharedPreferences feedlyOAuthPrefData;
    private Context context;
    private String feedly_accessToken = "";
    private String feedly_refreshToken = "";
    private String feedly_tokenType = "";
    private long feedly_expireTime = 0;
    private boolean failed = false;
    private Gson gson = new Gson();
    private OkHttpClient client = new OkHttpClient();

    /**
     * @param cxt
     */
    public FeedlyConnectivity(Context cxt) {
        context = cxt;
        feedlyOAuthPrefData = context.getSharedPreferences(FEEDLY_AUTH_PREF, 0);
        feedly_accessToken = feedlyOAuthPrefData.getString(FeedlyConstants.FEEDLY_ACCESS_TOKEN_PREF_KEY, null);
        feedly_refreshToken = feedlyOAuthPrefData.getString(FeedlyConstants.FEEDLY_REFRESH_TOKEN_PREF_KEY, null);
        feedly_tokenType = feedlyOAuthPrefData.getString(FeedlyConstants.FEEDLY_TOKEN_TYPE_PREF_KEY, null);
        feedly_expireTime = feedlyOAuthPrefData.getLong(FeedlyConstants.FEEDLY_EXPIRY_TIME_PREF_KEY, 0);
    }

    @NonNull
    public FeedlyUserProfile FeedlyUserProfile() {

        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(FeedlyConstants.getFeedlyUserProfileUrl())
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                String userProfile = response.body().string();
                Log.d("UserProfile", response.body().string());
                FeedlyUserProfile feedlyUserProfile = gson.fromJson(userProfile, FeedlyUserProfile.class);
                if (feedlyUserProfile != null) {
                    return feedlyUserProfile;
                }
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("UserProfileExp", "UserProfile_Exception\t" + exception.getMessage());

        }
        return null;
    }

    /**
     * @return will give you the Available Categories list.
     * @throws IOException
     */
    @NonNull
    public List<FeedlyCatagories> getFeedlyCatagories() {

        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(FeedlyConstants.getFeedlyCatagoriesUrl())
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                String catagories = response.body().string();
                Type feedlyCatagories = new TypeToken<Collection<FeedlyCatagories>>() {}.getType();
                List<FeedlyCatagories> allCatagories = gson.fromJson(catagories, feedlyCatagories);
                if (allCatagories != null) {
                    return allCatagories;
                }
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            failed = true;
            Log.d("JsonEx", exception.getMessage());
        }
        if (failed) {
            // if failed will restart the request
            getFeedlyCatagories();
        }
        return null;
    }

    /**
     * @return
     * @throws IOException
     */
    @NonNull
    public List<FeedlySubscription> getFeedlyUserSubscription() {

        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(FeedlyConstants.getFeedlySubscriptionUrl())
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                String subscription = response.body().string();
           /* Log.d("subReturnCode", subscription);
            Log.d("subReturnCode", getFeedly_accessToken());*/
                Type FeedlySubscriptionsType = new TypeToken<Collection<FeedlySubscription>>() {
                }.getType();
                List<FeedlySubscription> allSubscription = gson.fromJson(subscription, FeedlySubscriptionsType);
                if (allSubscription != null) {
                    return allSubscription;
                }
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            failed = true;
            Log.d("JsonEx", exception.getMessage());
        }
        if (failed) {
            //
            getFeedlyUserSubscription();
        }
        return null;
    }

    /**
     * @param feedId
     * @param title
     */
    @NonNull
    public boolean subscribeToFeedlyFeeds(String feedId, String title) {
        //Log.d("simpleLog",feedSubscriptionSpitter(feedId,title,categoryId,Categorylable))  ;
        JSONObject subscribePost = null;
        try {
            subscribePost = new JSONObject(feedSubscriptionSpitter(feedId, title));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), subscribePost.toString());
        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(FeedlyConstants.FEEDLY_SUBSCRIPTION_URL)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("NewSubscription", "code:\t" + response.code());
            if (response != null && response.code() == 200) {
                Log.d("NewSubscription", "result:\t" + response.body().string());
                return true;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("NewSubscription", "exception Message:\t" + exception.getMessage());
        }
        return false;
    }

    /***
     * @param feedId
     * @param title
     * @return
     */
    @NonNull
    private String feedSubscriptionSpitter(String feedId, String title) {
        return "{" + "\"title\":" + '"' + title + '"' + "," + "\"id\":" + '"' + feedId + '"' + "}";


    }

    @NonNull
    public String feedMassSubscription(String feedId, String title, ArrayList<FeedlySubscription.CategoriesBean> categoriesBeen) {
        return " {" + "  \"categories\":[" + '"' + categoriesBeen + '"' + "]," + " \"title\":" + '"' + title + '"' + "," + "\"id\":" + '"' + feedId + '"' + "}";
    }

    /**
     * @param feedId
     * @return
     */
    @NonNull
    public FeedlyFeedItems getFeedlyFeed(String feedId) {
        String httpurl = "";
        try {
            httpurl = FeedlyConstants.getFeedlyFeedUrl() + "/" + URLEncoder.encode(feedId, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(httpurl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                String feedItems = response.body().string();
                // Log.d("FeedlyFeed","Feed result:\t"+response.body().string());
                FeedlyFeedItems feedlyFeedItems = gson.fromJson(feedItems, FeedlyFeedItems.class);
                if (feedlyFeedItems != null)
                    return feedlyFeedItems;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("JsonEx", exception.getMessage());
        }
        return null;
    }

    /**
     * @param query
     * @param count
     * @param locale
     * @throws IOException
     */
    @NonNull
    public List<FeedlySearchResultItem.ResultsBean> feedlySearch(String query, String count, String locale) throws IOException {
        // Log.d("QueryApiTest",feedlySearchUrlSpitter(query,count,locale).url().toString());
        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(feedlySearchUrlSpitter(query, count, locale).url())
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("QueryApiTest", "code \t" + response.code());
            if (response != null && response.code() == 200) {
                // Log.d("QueryApiTest", "result %s\t" + response.body().string());
                String searchresult = response.body().string();
                FeedlySearchResultItem feedlySearchResultItem = gson.fromJson(searchresult, FeedlySearchResultItem.class);
                return feedlySearchResultItem.getResults();
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("searchException", exception.getMessage());
        }
        return null;
    }
/*
    public List<FeedlyStreamContent> getStreamContent(String StreamId){
        String httpurl="";
        try {
            httpurl = FeedlyConstants.getFeedlyEntriesIdUrl() + "/" + URLEncoder.encode(StreamId,"utf-8")+"/contents";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("StreamContent", "code \t" + httpurl);
        Request request = new Request.Builder()
                .header("Authorization",feedly_tokenType+" "+feedly_accessToken)
                .url(httpurl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("StreamContent", "code %s\t" + response.code());
            if (response != null && response.code() == 200) {
                String streamcontent=response.body().toString();
                Log.d("StreamContent", streamcontent);
                Type FeedlyStreamContent = new TypeToken<Collection<FeedlyStreamContent>>(){}.getType();
                List<FeedlyStreamContent> allStreamContent =  gson.fromJson(streamcontent,FeedlyStreamContent);

                if (allStreamContent!=null)
                    return allStreamContent;
            }
        }catch (IllegalStateException | JsonSyntaxException  | IOException exception) {
            Log.d("StreamContent","StreamContent_exp\t"+exception.getMessage());
        }
        return  null;
    }*/

    /**
     *
     */
    @NonNull
    public FeedlyEntryIdsListOfStream getFeedlyFeedEntryIds(String streamId, int count) {

        String httpurl = "";
        try {
//            httpurl = FeedlyConstants.getFeedlyEntriesIdUrl() + "/ids?streamId=" + URLEncoder.encode(streamId,"utf-8")+"&count=10";
            httpurl = FeedlyConstants.getFeedlyEntriesIdUrl() + "/ids?streamId=" + URLEncoder.encode(streamId, "utf-8") + "&count=" + count;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("EntryIds", "code \t" + httpurl);
        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(httpurl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("EntryIds", "code %s\t" + response.code());
            if (response != null && response.code() == 200) {
                String entries = response.body().string();
                // Log.d("EntryIds", String.format("Entry_Ids:%s",entries));

                FeedlyEntryIdsListOfStream feedlyEntryIdsListOfStream = gson.fromJson(entries, FeedlyEntryIdsListOfStream.class);
                return feedlyEntryIdsListOfStream;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("EntryIds", exception.getMessage());
        }
        return null;
    }


    public void getFeedlylistOfFeeds(List<String> feedIds) {

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new JSONArray(feedIds).toString());
        Log.d("FeedlylistOfFeeds", "requestBody\t" + requestBody.toString());
        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(FeedlyConstants.FEEDLY_FEED_LIST_URL)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("FeedlylistOfFeeds", String.valueOf(response.code()));
            if (response != null && response.code() == 200) {
                String listOfFeeds = response.body().toString();
                Log.d("FeedlylistOfFeeds", listOfFeeds);
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("FeedlylistOfFeeds", exception.getMessage());
        }

    }


    private String SpitListOFFeedIds(String[] feedIds) {
        return "[" + feedIds + "]";
    }


    /***
     * @param entryIds
     * @return
     */

    public List<FeedlyDynamicEntryList> getContentOfDynamicEntryList(List<String> entryIds) {
/*
          Log.d("logOfEntryIds",entryIds.toString());*/
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new JSONArray(entryIds).toString());
        Log.d("DynamicNewEntries", "requestBody\t" + requestBody.toString());
        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(FeedlyConstants.FEEDLY_DYNAMIC_ENTRY_LIST_URL)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("DynamicNewEntries", "code:\t" + response.code());
            if (response != null && response.code() == 200) {
                String dynamicentries = response.body().string();
                // Log.d("DynamicNewEntries", "code:\t" + response.body().string());
                Type FeedlyDynamicEntryList = new TypeToken<Collection<FeedlyDynamicEntryList>>() {
                }.getType();
                List<FeedlyDynamicEntryList> feedlyDynamicEntryList = gson.fromJson(dynamicentries, FeedlyDynamicEntryList);
                return feedlyDynamicEntryList;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("DynamicNewEntries", exception.getMessage());
        }
        return null;
    }

    /**
     * @param streamId
     * @param unreadOnly
     * @param showNewest
     * @param count
     * @return Will give array of entry ids and we will use it to get dynamic entry list maximum count is 1000
     */
    public FeedlyEntryIdsListOfStream getDynamicEntriesList(String streamId, boolean unreadOnly, boolean showNewest, int count) {
        String httpurl = "";
        try {
            httpurl = FeedlyConstants.getFeedlyEntriesIdUrl() + "/ids?streamId=" + URLEncoder.encode(streamId, "utf-8") + "&unreadOnly=" + unreadOnly + "&count=" + count + "&ranked=" + (showNewest ? "newest" : "oldest");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("DynamicEntriesList", "code\t" + httpurl);
        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(httpurl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("DynamicEntriesList", "code\t" + response.code());
            if (response != null && response.code() == 200) {
                String entries = response.body().string();
                //Log.d("DynamicEntriesList", String.format("DynamicEntriesList:%s\t",entries));
                FeedlyEntryIdsListOfStream feedlyEntryIdsListOfStream = gson.fromJson(entries, FeedlyEntryIdsListOfStream.class);
                return feedlyEntryIdsListOfStream;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("DynamicEntriesList", "Exception_From_DynamicEntriesList:-\t" + exception.getMessage());
        }

        return null;

    }

    /**
     * @param entryId
     * @return
     */
    public List<FeedlyEntries> getEntryContent(String entryId) {
        String httpurl = "";
        try {
            httpurl = FeedlyConstants.getFeedlyEntriesUrl() + "/" + URLEncoder.encode(entryId, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("EntryContent", "code \t" + httpurl);
        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(httpurl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("EntryContent", "code\t" + response.code());
            if (response != null && response.code() == 200) {
                String entries = response.body().string();
                // Log.d("EntryContent", String.format("EntryContent:%s",entries));
                Type FeedlyEntries = new TypeToken<Collection<FeedlyEntries>>() {
                }.getType();
                List<FeedlyEntries> feedlyEntries = gson.fromJson(entries, FeedlyEntries);
                return feedlyEntries;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("EntryIds", exception.getMessage());
        }
        return null;
    }

    /**
     * @param id
     * @return
     */
    public List<FeedlyUnreadCount.UnreadcountsBean> getGenericCountOfUnreadArticles(String id) {
        //source=mailto&to=developers@feedly.com
        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(FeedlyConstants.getFeedlyCountUrl())
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("UnReadCount", "code\t" + response.code());
            if (response != null && response.code() == 200) {

                Log.d("UnReadCount", String.valueOf(response.code()));
                // Log.d("UnReadCount",response.body().string());
                String unReadCount = response.body().string();
                FeedlyUnreadCount feedlyUnreadCount = gson.fromJson(unReadCount, FeedlyUnreadCount.class);
                if (feedlyUnreadCount != null) {
                    return feedlyUnreadCount.getUnreadcounts();
                }
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("UnReadCount", exception.getMessage());
        }
        return null;
    }

    /**
     * @param Id
     * @return it will give the count of  the number of unread articles for an Id (Id may be a feed, subscription, category or tag)
     */
    public int getCountOfUnreadArticles(String Id) {
        //source=mailto&to=developers@feedly.com
        Request request = new Request.Builder()
                .header("Authorization", feedly_tokenType + " " + feedly_accessToken)
                .url(FeedlyConstants.getFeedlyCountUrl())
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("UnReadCount", "code\t" + response.code());
            if (response != null && response.code() == 200) {

                Log.d("UnReadCount", String.valueOf(response.code()));
                // Log.d("UnReadCount",response.body().string());
                String unReadCount = response.body().string();
                FeedlyUnreadCount feedlyUnreadCount = gson.fromJson(unReadCount, FeedlyUnreadCount.class);
                if (feedlyUnreadCount != null) {
                    for (FeedlyUnreadCount.UnreadcountsBean unreadCount : feedlyUnreadCount.getUnreadcounts()) {
                        if (Id.equals(unreadCount.getId())) {
                            return unreadCount.getCount();
                        }
                    }
                }
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("UnReadCount", exception.getMessage());
        }
        return 0;
    }

    public void markAsRead(String id, String type) {

        JSONObject jObject = new JSONObject();

        try {
            jObject.put("action", "recentlyReadEntries");
            jObject.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray ids = new JSONArray();
        ids.put(id);

        String typeIdIdentificator = null;

        if (type.equals("entries")) {
            typeIdIdentificator = "entryIds";
        } else if (type.equals("feeds")) {
            typeIdIdentificator = "feedIds";
        } else if (type.equals("categories")) {
            typeIdIdentificator = "categoryIds";
        } else {
            Log.d("recentlyReadEntries:", " Unknown type: " + type + " don't know what to do with this.");
        }
        try {
            jObject.put(typeIdIdentificator, ids);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jObject.toString());
        Log.d("recentlyReadEntries", "requestBody\t" + requestBody.toString());
        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(FeedlyConstants.FEEDLY_MARK_AS_READ_URL)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("recentlyReadEntries", "code:\t" + response.code());
            if (response != null && response.code() == 200) {
/*
                Log.d("recentlyReadEntries",String.valueOf(response.code()));
*/
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("recentlyReadEntries", "exception Message:\t" + exception.getMessage());
        }

    }

    /**
     * @param query
     * @param count
     * @param locale
     * @return
     */
    @NonNull
    public HttpUrl feedlySearchUrlSpitter(String query, String count, String locale) {
        return Feedly_Search_Parameter.newBuilder()
                .setQueryParameter("q", query)
                .setQueryParameter("n", count)
                .setQueryParameter("l", locale)
                .build();
    }

    /**
     * @param label
     * @return will change the Label of the Existing Category
     */
    @NonNull
    public boolean editCategoryLabel(String categoryId, String label) {
        JSONObject labelpost = null;
        String httpurl = null;
        try {
            labelpost = new JSONObject(labelSpitter(label));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            httpurl = FeedlyConstants.FEEDLY_CATAGORIES_URL + "/" + URLEncoder.encode(categoryId, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), labelpost.toString());
        Log.d("editCatLabel", String.format("editCategoryLabel: httpurl is %s ", httpurl.toString() + "Json  " + labelpost.toString()));
        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(httpurl)
                .post(requestBody)
                .build();
        Log.d("editCatLabel", "Category Id:\t" + categoryId);
        try {
            Response response = client.newCall(request).execute();
            Log.d("editCatLabel", "code:\t" + response.code());
            if (response != null && response.code() == 200) {
                Log.d("editCatLabel", "CatEditResult:\t" + response.body().string());
                return true;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("editCatLabel", "exception Message:\t" + exception.getMessage());
        }
        return false;
    }

    /**
     * @param categoryId
     * @return delete existing Category
     */
    @NonNull
    public boolean deleteFeedlyCategory(String categoryId) {
        String httpUrl = null;
        try {
            httpUrl = FeedlyConstants.FEEDLY_CATAGORIES_URL + "/" + URLEncoder.encode(categoryId, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .header("Authorization", "OAuth" + " " + getFeedly_accessToken())
                .url(httpUrl)
                .delete()
                .build();
        Log.d("deleteCategory", "category id:\t" + categoryId);
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                Log.d("deleteCategory", "categoryDeleteResult:\t" + response.body().string());
                return true;
            }
        } catch (IllegalStateException | JsonSyntaxException | IOException exception) {
            Log.d("deleteCategory", "exception Message:\t" + exception.getMessage());
        }
        return false;
    }

    @NonNull
    private String labelSpitter(String label) {
        return "{" + "\"label\":" + '"' + label + '"' + "}";
    }


    public String getFeedly_accessToken() {
        return feedly_accessToken;
    }

    public String getFeedly_refreshToken() {
        return feedly_refreshToken;
    }

    public String getFeedly_tokenType() {
        return feedly_tokenType;
    }

    public long getFeedly_expireTime() {
        return feedly_expireTime;
    }
}
