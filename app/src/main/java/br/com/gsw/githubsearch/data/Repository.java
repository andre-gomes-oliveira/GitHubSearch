package br.com.gsw.githubsearch.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Repository implements Parcelable {

    private final int mId;
    private final String mName;
    private final String mAvatarUrl;
    private final String mDescription;
    private final String mLink;

    public Repository(int id, String name, String avatarUrl, String description, String link) {
        this.mId = id;
        this.mName = name;
        this.mAvatarUrl = avatarUrl;
        this.mDescription = description;
        this.mLink = link;
    }

    private Repository(Parcel in)
    {
        this.mId = in.readInt();
        this.mName = in.readString();
        this.mAvatarUrl = in.readString();
        this.mDescription = in.readString();
        this.mLink = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mAvatarUrl);
        dest.writeString(mDescription);
        dest.writeString(mLink);
    }

    public int getId() { return mId; }

    public String getName()
    {
        return mName;
    }

    public String geAvatarUrl()
    {
        return mAvatarUrl;
    }

    public String getDescription()
    {
        return mDescription;
    }

    public String getLink()
    {
        return mLink;
    }

    public static final Parcelable.Creator<Repository> CREATOR = new Parcelable.Creator<Repository>()
    {
        @Override
        public Repository createFromParcel(Parcel parcel)
        {
            return new Repository(parcel);
        }

        @Override
        public Repository[] newArray(int i) {
            return new Repository[i];
        }
    };
}
