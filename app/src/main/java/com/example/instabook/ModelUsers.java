package com.example.instabook;

public class ModelUsers
{
    String name,email,search,phone, image,uid;

    public ModelUsers()
    {

    }

    public ModelUsers(String name,String email, String search, String phone, String image,String uid)
    {
        this.name=name;
        this.email=email;
        this.search=search;
        this.phone=phone;
        this.image=image;
        this.uid=uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getSearch() {
        return search;
    }

    public String getImage() {
        return image;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSearch(String search) {
        this.search = search;
    }

}
