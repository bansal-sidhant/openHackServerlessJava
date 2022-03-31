package com.product.rating;

public class Rating {
    private String id;
    private String userId;
    private String productId;
    private String timeStamp;
    private String locationName;
    private int rating;
    private String userNotes;
    
    public Rating(String id, String userId, String productId, String timeStamp, String locationName, int rating,
            String userNotes) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.timeStamp = timeStamp;
        this.locationName = locationName;
        this.rating = rating;
        this.userNotes = userNotes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    @Override
    public String toString() {
        return "Rating [id=" + id + ", locationName=" + locationName + ", productId=" + productId + ", rating=" + rating
                + ", timeStamp=" + timeStamp + ", userId=" + userId + ", userNotes=" + userNotes + "]";
    }


}
