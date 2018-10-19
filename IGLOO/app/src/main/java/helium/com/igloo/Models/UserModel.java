package helium.com.igloo.Models;

import java.util.List;

public class UserModel {

    private String name;
    private String email;
    private String username;
    private String password;
    private String profileUrl;
    private int tokens;
    private double rating;
    private double numberOfSubscribers;
    private List<String> subscriptions;
    private double numberOfLectures;
    private double totalRatings;
    private int numberOfRatings;
    private List<String> tags;

    public UserModel(){

    }

    public UserModel(String name, String email, String password, String profileUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileUrl = profileUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public double getTokens() { return tokens; }

    public void setTokens(int tokens) { this.tokens = tokens; }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getNumberOfSubscribers() {
        return numberOfSubscribers;
    }

    public void setNumberOfSubscribers(double numberOfSubscribers) {
        this.numberOfSubscribers = numberOfSubscribers;
    }

    public List<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public double getNumberOfLectures() {
        return numberOfLectures;
    }

    public void setNumberOfLectures(double numberOfLectures) {
        this.numberOfLectures = numberOfLectures;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public double getTotalRatings(){ return this.totalRatings;}

    public void setTotalRatings(double totalrating){ this.totalRatings = rating;}

    public int getNumberOfRatings(){return this.numberOfRatings;}

    public void setNumberOfRatings(int numberOfRatings){this.numberOfRatings = numberOfRatings;}
}
