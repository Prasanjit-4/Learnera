package com.semicolon.learnera;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class UserPost implements Parcelable {
    public String courseLink,description,thumbPost,userID;
    public Date postTimeStamp;


    protected UserPost(Parcel in) {
        courseLink = in.readString();
        description = in.readString();
        thumbPost = in.readString();
        userID = in.readString();
    }

    public static final Creator<UserPost> CREATOR = new Creator<UserPost>() {
        @Override
        public UserPost createFromParcel(Parcel in) {
            return new UserPost(in);
        }

        @Override
        public UserPost[] newArray(int size) {
            return new UserPost[size];
        }
    };

    public Date getPostTimeStamp() {
        return postTimeStamp;
    }

    public void setPostTimeStamp(Date postTimeStamp) {
        this.postTimeStamp = postTimeStamp;
    }

    public UserPost(){}
    public UserPost(String courseLink, String description, String thumbPost, String userID,Date postTimeStamp) {
        this.courseLink = courseLink;
        this.description = description;
        this.thumbPost = thumbPost;
        this.userID = userID;
        this.postTimeStamp = postTimeStamp;
    }

    public String getCourseLink() {
        return courseLink;
    }

    public void setCourseLink(String courseLink) {
        this.courseLink = courseLink;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbPost() {
        return thumbPost;
    }

    public void setThumbPost(String thumbPost) {
        this.thumbPost = thumbPost;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseLink);
        dest.writeString(description);
        dest.writeString(thumbPost);
        dest.writeString(userID);
    }
}
