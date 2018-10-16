package helium.com.igloo.Models;

public class SubscriptionModel {
    private String streamer;
    private String subscriber;
    private String status;

    public  SubscriptionModel(){}
    public SubscriptionModel(String streamer, String subscriber, String status) {
        this.streamer = streamer;
        this.subscriber = subscriber;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
