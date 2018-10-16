package helium.com.igloo.Models;

public class SubscriptionModel {
    private String streamer;
    private String subscriber;

    public  SubscriptionModel(){}
    public SubscriptionModel(String streamer, String subscriber) {
        this.streamer = streamer;
        this.subscriber = subscriber;
    }

    public String getStreamer() {
        return streamer;
    }

    public void setStreamer(String streamer) {
        this.streamer = streamer;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }
}
