package com.example.instabook;

public class ModelPost {
    String titleStr, descriptionStr,likesStr, numOfCommentsStr, imageStr, uidStr, emailStr, userNameStr, userPicStr, timeStr;

    public ModelPost()
    {

    }
    public ModelPost(String userPicStr, String numOfCommentsStr, String likesStr, String timeStr, String titleStr, String descriptionStr, String imageStr, String uidStr, String emailStr, String userNameStr) {
        this.titleStr = titleStr;
        this.descriptionStr = descriptionStr;
        this.imageStr = imageStr;
        this.uidStr = uidStr;
        this.emailStr = emailStr;
        this.userNameStr = userNameStr;
        this.userPicStr=userPicStr;
        this.timeStr=timeStr;
        this.likesStr=likesStr;
        this.numOfCommentsStr=numOfCommentsStr;
    }

    public void setNumOfCommentsStr(String numOfCommentsStr)
    {
        this.numOfCommentsStr=numOfCommentsStr;
    }

    public String getNumOfCommentsStr()
    {
        return this.numOfCommentsStr;
    }

    public String getLikesStr()
    {
        return this.likesStr;
    }

    public void setLikesStr(String likesStr)
    {
        this.likesStr=likesStr;
    }

    public String getTimeStr()
    {
        return this.timeStr;
    }

    public void setTimeStr(String timeStr)
    {
        this.timeStr=timeStr;
    }

    public String getUserPicStr()
    {
        return this.userPicStr;
    }

    public void setUserPicStr(String userPicStr)
    {
        this.userPicStr=userPicStr;
    }



    public String getTitleStr() {
        return titleStr;
    }

    public String getDescriptionStr() {
        return descriptionStr;
    }

    public String getImageStr() {
        return imageStr;
    }

    public String getUidStr() {
        return uidStr;
    }

    public String getEmailStr() {
        return emailStr;
    }

    public String getUserNameStr() {
        return userNameStr;
    }


    public void setTitleStr(String titleStr) {
        this.titleStr = titleStr;
    }

    public void setDescriptionStr(String descriptionStr) {
        this.descriptionStr = descriptionStr;
    }

    public void setImageStr(String imageStr) {
        this.imageStr = imageStr;
    }

    public void setUidStr(String uidStr) {
        this.uidStr = uidStr;
    }

    public void setEmailStr(String emailStr) {
        this.emailStr = emailStr;
    }

    public void setUserNameStr(String userNameStr) {
        this.userNameStr = userNameStr;
    }
}
