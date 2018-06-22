package com.biniam.rss.connectivity.feedbin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.biniam.rss.connectivity.feedbin.feedbinUtils.FeedbinUrls;
import com.biniam.rss.models.feedbin.FeedBinEntriesItem;
import com.biniam.rss.models.feedbin.FeedBinSubscriptionsItem;
import com.biniam.rss.models.feedbin.FeedBinTaggingsItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *   Created by biniam on 2/3/17.
 *
 */

public class FeedbinConnectify {

    // TODO: 3/4/17  Deleting Subscription and Deleting Tag are not Working Properly.
    // TODO: 3/4/17  Check the Method that adds a new subscription and the Respond code that it returns


    private static final String  TAG = FeedbinConnectify.class.getSimpleName();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    Gson gson = new Gson();
    HashMap<String, String> UserCredentialMap = new HashMap();
    private boolean isUserAuthenticated = false;
    private int     UserOK = 200;
    private int     UserIsNotAllowed = 403;

    /**
     *
     * @return it returns True if the user is Authorized and False is else
     */

    @NonNull
    public boolean UserAuthentication(Context context) throws IOException {


        Response response = respondSpitter(FeedbinUrls.getAuthentication_URL(), context);
        if (response != null) {
            if (response.code() == UserOK) {
                isUserAuthenticated = true;
                Log.d("Authorized user Login", String.valueOf(isUserAuthenticated));
            } else if (response.code() == UserIsNotAllowed) {
                isUserAuthenticated = false;
                Log.d("UnAuthorized User Login", String.valueOf(isUserAuthenticated));
            } else {
                isUserAuthenticated = false;
            }
        }
        return isUserAuthenticated;
    }


    public boolean isUserValid(String username, String password) {
        Request request = new Request.Builder()
                .addHeader("Authorization", Credentials.basic(username, password))
                .url(FeedbinUrls.getAuthentication_URL()).build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                Log.d(TAG, "isUserValid: " + response.code() + "the response body..\t" + response.body().string());
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getUserCred(String s) {
        UserCredentialMap.put("user", s);
        return s;
    }

    private void getPass(String s) {
        UserCredentialMap.put("pass", s);
    }

    /**
     * @return when the username and password are given and the method will give the credentials
     * @param context
     */
    @NonNull
    private String spitCredentials(Context context) {

//        SecureAccountManager secureAccountManager = new SecureAccountManager.Builder(context).build();
//        secureAccountManager.deObfuscateUsername()
//                .observeOn(Schedulers.io())
//                .subscribe(s -> getUserCred(s));
//
//        secureAccountManager.deObfuscatePassword()
//                .observeOn(Schedulers.io())
//                .subscribe(s -> getPass(s));
//        if (UserCredentialMap.get("user") != null && UserCredentialMap.get("pass") != null) {
//            String credentials = Credentials.basic(UserCredentialMap.get("user"), UserCredentialMap.get("pass"));
//            return credentials;
//        }
        return "";
    }
    /**
     * @return
     * @throws IOException
     */
    @NonNull
    public List<FeedBinSubscriptionsItem> getAllUserSubScriptions(Context context) throws IOException {
        Response response = respondSpitter(FeedbinUrls.getSubscriptionUrl(), context);
        if (response != null) {
            String SubscriptionData = response.body().string();
            Type FeedBinSubscriptionsType = new TypeToken<Collection<FeedBinSubscriptionsItem>>() {
            }.getType();
            List<FeedBinSubscriptionsItem> SubscriptionListdata = gson.fromJson(SubscriptionData, FeedBinSubscriptionsType);
            return SubscriptionListdata;
        }
        return null;
    }

    /**
     *
     * @param Id
     * @param context
     * @return
     * @throws IOException
     */
    @NonNull
    public FeedBinSubscriptionsItem getSubscriptionById(int Id, Context context) throws IOException {
        String SubscriptionData="";
        Response response = respondSpitter(FeedbinUrls.getSubscriptionAddUrl() + Id + ".json", context);
        if (response!=null)
            SubscriptionData = response.body().string();
        FeedBinSubscriptionsItem feedBinSubscriptionsItem = gson.fromJson(SubscriptionData, FeedBinSubscriptionsItem.class);
        return feedBinSubscriptionsItem;
    }

    /**
     *
     * @param context
     * @param feed_url
     * @return
     * @throws IOException
     */
    @NonNull
    public String addNewFeedbinSubscription(Context context, String feed_url) throws IOException {

        String newSubscriptionSuccessful="";
        RequestBody body = RequestBody.create(JSON, jsonUrlSpitter(feed_url));

        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getSubscriptionUrl())
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response != null) {
            if (response.code() == 201) {
                newSubscriptionSuccessful = String.valueOf(response.code());
            } else if (response.code() == 302) {
                newSubscriptionSuccessful = response.body().string();
            } else if (response.code() == 300) {
                newSubscriptionSuccessful = response.body().string();
            } else if (response.code() == 404) {
                newSubscriptionSuccessful = response.body().string();
            }
        } else {
            newSubscriptionSuccessful = String.valueOf(response.code());
        }
        return newSubscriptionSuccessful;


    }

    /**
     *
     * @param url
     * @return
     */
    @NonNull
    private String jsonUrlSpitter(String url){
        return "{\n" + " \"feed_url\":" + '"' + url + '"' + " " + "}";
    }

    /**
     * @param customTitle
     * @return will return a Custom title like a Json
     */
    @NonNull
    private String jsonCustomTitleSpitter(String customTitle){
        return "{\n" +
                "  \"title\":"+'"'+customTitle +'"' +
                "}";
    }

    /**
     *
     * @param custom_title
     * @param sub_id
     * @param context
     * @return
     * @throws IOException
     */
    public FeedBinSubscriptionsItem updategeedBinSubscription(String custom_title, int sub_id, Context context) throws IOException {

        RequestBody body = RequestBody.create(JSON, jsonCustomTitleSpitter(custom_title));
        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getSubscriptionUpdate() + sub_id + "/update.json")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        if (response != null && response.code() != 403) {
            FeedBinSubscriptionsItem feedBinSubscriptionsItem = gson.fromJson(response.body().string(), FeedBinSubscriptionsItem.class);
            return feedBinSubscriptionsItem;
        }
        return null;
    }

    /**
     * @param sub_Id
     * @return will delete a Subscription using subscription Id
     * @throws IOException
     */
    @NonNull
    public boolean deleteFeedBinSubscription(int sub_Id, Context context) throws IOException {

        boolean isSubscriptionDeleted = false;
        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getSubscriptionAddUrl() + sub_Id + ".json")
                .delete()
                .build();
        Response response = client.newCall(request).execute();
        if (response!=null){
            if (response.code()==204) {
                isSubscriptionDeleted = true;
            }else if(response.code()==403){
                isSubscriptionDeleted = false;
            }
        }
        return isSubscriptionDeleted;
    }
    /**
     * @return get all the Entries of the Feeds
     */
    @NonNull
    public List<FeedBinEntriesItem> getAllFeedEntries(Context context) throws IOException {
        Response response = respondSpitter(FeedbinUrls.getEntriesUrl(), context);
        if (response == null) {
            return null;
        }
        Type FeedBinEntriesItemType = new TypeToken<Collection<FeedBinEntriesItem>>() {
        }.getType();
        List<FeedBinEntriesItem> EntriesListdata = gson.fromJson(response.body().string(), FeedBinEntriesItemType);
        return EntriesListdata;
    }

    /**
     * @param feed_id
     * @return get the feed  with specific id
     * @throws IOException
     */
    @NonNull
    public FeedBinSubscriptionsItem getFeed(int feed_id, Context context) throws IOException {
        Response response = respondSpitter(FeedbinUrls.getFeedUrl() + feed_id + ".json", context);
        if (response != null) {
            FeedBinSubscriptionsItem feedFromEntriesByFeedId = gson.fromJson(response.body().string(), FeedBinSubscriptionsItem.class);
            return feedFromEntriesByFeedId;
        }
        return null;
    }

    /**
     * @param feed_id  feed id in this case is a subscription id
     * @return
     * @throws IOException
     */
    //Still not tested
    @NonNull
    public List<FeedBinEntriesItem> getEntryByFeedId(int feed_id, Context context) throws IOException {
        Response response = respondSpitter(FeedbinUrls.getFeedUrl() + feed_id + "/entries.json", context);
        if (response != null) {
            String entriesByFeedId = response.body().string();
            Type FeedBinEntriesItemType = new TypeToken<Collection<FeedBinEntriesItem>>() {
            }.getType();
            List<FeedBinEntriesItem> entriesByfeedId = gson.fromJson(entriesByFeedId, FeedBinEntriesItemType);
            return entriesByfeedId;
        }
        return null;
    }

    /**
     * @param entry_id
     * @return
     * @throws IOException
     */
    public FeedBinEntriesItem getEntryByEntryId(int entry_id, Context context) throws IOException {
        Response response = respondSpitter(FeedbinUrls.getENTRIES() + entry_id + ".json", context);
        if (response != null) {
            String entrydata = response.body().string();
            FeedBinEntriesItem feedFromEntriesByEntrydId = gson.fromJson(entrydata, FeedBinEntriesItem.class);
            return feedFromEntriesByEntrydId;
        }
        return null;
    }

    /**
     * @return it will give only 100 entry_ids at a time
     * @throws IOException
     */
    public List<Integer> getUnreadEntries(Context context) {
        Response response = respondSpitter(FeedbinUrls.getUnreadEntriesUrl(), context);
        if (response != null) {
            try {
                String UnreadEntries = response.body().string();
                try {
                    JSONArray array = new JSONArray(UnreadEntries);
                    ArrayList<Integer> unreadEntriesId = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++)
                        unreadEntriesId.add(array.getInt(i));
                    return unreadEntriesId;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    // TODO: 8/30/17  this is not fully done so test it before using it
    public List<Integer> markEntriesAsRead(List<Integer> entriesId, Context context) {
        RequestBody body = RequestBody.create(JSON, spiteUnreadIds(entriesId));
        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getUnreadEntriesUrl())
                .delete(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (request != null && response.code() == 200) {
                String markedIds = response.body().string();
                Log.d(TAG, "getRecentlyRead: " + markedIds);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * will give the starred entry id in list<Integer> format
     *
     * @return
     */
    public List<Integer> getStarredEntries(Context context) {
        Response response = respondSpitter(FeedbinUrls.getStarredUrl(), context);
        if (response != null && response.code() == 200) {
            try {
                String mStarredEntries = response.body().string();
                try {
                    JSONArray array = new JSONArray(mStarredEntries);
                    ArrayList<Integer> starredIds = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++)
                        starredIds.add(array.getInt(i));
                    return starredIds;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     * @param context
     * @return
     * @throws IOException
     */
    @NonNull
    public List<FeedBinTaggingsItem> getAllTages(Context context) throws IOException {
        Response response = respondSpitter(new FeedbinUrls().getTagUrl(), context);
        if (response != null) {
            String Tages = response.body().string();
            // Log.d(TAG, "getAllTages: "+Tages);
            Type FeedBinSubscriptionsType = new TypeToken<Collection<FeedBinTaggingsItem>>() {
            }.getType();
            List<FeedBinTaggingsItem> allTages = gson.fromJson(Tages, FeedBinSubscriptionsType);
            return allTages;
        }
        return null;
    }
    /**
     * @param tag_id
     * @return get the Tag  using an id which is the tag_id
     */
    @NonNull
    public FeedBinTaggingsItem getTageWithId(int tag_id, Context context) throws IOException {
        Response response = respondSpitter(FeedbinUrls.getSingleTag() + tag_id + ".json", context);
        if (response != null) {
            String TagWithId = response.body().string();
            FeedBinTaggingsItem tagFoundbyId = gson.fromJson(TagWithId, FeedBinTaggingsItem.class);
            return tagFoundbyId;
        }
        return null;
    }

    /**
     *
     * @param tag_id
     * @param context
     * @return
     * @throws IOException
     */
    @NonNull
    public boolean deleteTagById(int tag_id, Context context) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getTagDeleteUrl() + tag_id + ".json")
                .delete()
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.code() == 204) {
            Log.d("TagDeleted", String.valueOf(response.code()));
            return true;
        }
        return false;
    }

    /**
     *
     * @param tageName
     * @param feed_id
     * @param context
     * @return
     * @throws IOException
     */
    @NonNull
    public String createNewFeedbinTag(String tageName, int feed_id, Context context) throws IOException {
        String newTagUrl="";
        RequestBody jsonBody = RequestBody.create(JSON,newTagJsonSpitter(feed_id,tageName));
        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getSubscriptionUrl())
                .post(jsonBody)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null) {
            if (response.code() == 201) {
                newTagUrl = response.header("Location");
                //Utils.showToast(context,"New tagging is successful.");
            } else if (response.code() == 302) {
                newTagUrl = response.header("Location");
            }
        }
        return newTagUrl;
    }

    /**
     *
     * @param entry_ids
     * @param context
     * @return
     */
    public List<Integer> markstarredEntries(List<Integer> entry_ids, Context context) {
        RequestBody jsonBody = RequestBody.create(JSON, spitEntriesAsStarred(entry_ids));
        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getStarredUrl())
                .post(jsonBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    ArrayList<Integer> starreEntriesSuccessfull = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++)
                        starreEntriesSuccessfull.add(jsonArray.getInt(i));
                    return starreEntriesSuccessfull;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param starredEntries
     * @param context
     * @return
     */
    public boolean unStar(List<Integer> starredEntries, Context context) {
        RequestBody jsonBody = RequestBody.create(JSON, spitEntriesAsStarred(starredEntries));
        Request request = new Request.Builder()
                .addHeader("Authorization", spitCredentials(context))
                .url(FeedbinUrls.getStarredDeleteUrl())
                .post(jsonBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.code() == 200) {
                return true;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param feed_id
     * @param tagName
     * @return
     */
    @NonNull
    private String newTagJsonSpitter(int feed_id, String tagName){
        return "{\n" +
                "  \"feed_id\":" +feed_id+
                "  \"name\": "   +'"'+tagName +'"'+
                "}";
    }

    /**
     *
     * @param entriesIds
     * @return
     */
    private String spiteUnreadIds(List<Integer> entriesIds) {
        return "{\n" + "\"unread_entries\":" + entriesIds+"}";
    }

    /**
     *
     * @param entryId
     * @return
     */
    private String unReadIds(List<Integer> entryId) {

        JSONObject jsonOb = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < entryId.size(); i++) {
            jsonArray.put(entryId.get(i));
        }
        try {
            jsonOb.put("unread_entries", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonOb.toString();
    }

    /**
     * @param entry_ids
     * @return
     */
    private String spitEntriesAsStarred(List<Integer> entry_ids) {
        return "{\n" + "\"starred_entries\":" + entry_ids + "}";
    }

    /**
     *
     * @param url
     * @param context
     * @return
     */
    @NonNull
    private Response respondSpitter(String url, Context context) {
        if (!spitCredentials(context).isEmpty() || !spitCredentials(context).equals("") || spitCredentials(context) != null) {
            Request request = new Request.Builder()
                    .addHeader("Authorization", spitCredentials(context))
                    .url(url).build();
            try {
                Response response = client.newCall(request).execute();
                if (response != null && response.code() != 200) {
                    Log.d("Connection_Failed", String.valueOf(response.code()));
                } else {
                    return response;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
