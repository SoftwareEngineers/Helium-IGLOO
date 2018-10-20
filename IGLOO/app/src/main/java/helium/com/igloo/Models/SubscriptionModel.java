package helium.com.igloo.Models;

public class SubscriptionModel {
    private String streamer;
    private String subscriber;
    private String streamer_id;
    private String subscriber_id;
    private String status;

    public  SubscriptionModel(){}

    public SubscriptionModel(String streamer, String subscriber, String streamer_id, String subscriber_id , String status) {
        this.streamer = streamer;
        this.subscriber = subscriber;
        this.streamer_id = streamer_id;
        this.subscriber_id = subscriber_id;
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

    public String getStreamer_id() {
        return streamer_id;
    }

    public void setStreamer_id(String streamer_id) {
        this.streamer_id = streamer_id;
    }

    public String getSubscriber_id() {
        return subscriber_id;
    }

    public void setSubscriber_id(String subscriber_id) {
        this.subscriber_id = subscriber_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
