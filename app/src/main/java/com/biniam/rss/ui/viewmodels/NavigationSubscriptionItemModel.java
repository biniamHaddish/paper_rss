package com.biniam.rss.ui.viewmodels;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;

public class NavigationSubscriptionItemModel implements Parcelable {

    public static final Creator<NavigationSubscriptionItemModel> CREATOR = new Creator<NavigationSubscriptionItemModel>() {
        @Override
        public NavigationSubscriptionItemModel createFromParcel(Parcel in) {
            return new NavigationSubscriptionItemModel(in);
        }

        @Override
        public NavigationSubscriptionItemModel[] newArray(int size) {
            return new NavigationSubscriptionItemModel[size];
        }
    };
    public String id;
    public String title;
    public String iconUrl;
    @Ignore
    public int count;

    public NavigationSubscriptionItemModel() {
    }

    protected NavigationSubscriptionItemModel(Parcel in) {
        id = in.readString();
        title = in.readString();
        iconUrl = in.readString();
        count = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(iconUrl);
        parcel.writeInt(count);
    }
}