package com.example.instabook;

public class ModelComment {
    String cId, comment, timeStamp, uid, userEmail, userName,userImage;

    public ModelComment()
    {

    }

    public ModelComment(String cId,String userImage, String comment, String timeStamp, String uid, String userEmail, String userName)
    {
        this.cId=cId;
        this.comment=comment;
        this.timeStamp=timeStamp;
        this.uid=uid;
        this.userEmail=userEmail;
        this.userName=userName;
        this.userImage=userImage;

    }

    public String getUserImage()
    {
        return this.userImage;
    }

    public void setUserImage(String userImage)
    {
        this.userImage=userImage;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getcId() {
        return cId;
    }

    public String getComment() {
        return comment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }
}
