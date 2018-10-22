package helium.com.igloo.Models;

import java.io.Serializable;

public class NotificationModel implements Serializable{


    private String streamer;
    private String subscriber;
    private String streamer_id;
    private String subscriber_id;
    private String status;
    private String notification_title;
    private String notification_description;
    private String time_created;

    public  NotificationModel(){ }

    public NotificationModel(String streamer, String subscriber, String streamer_id, String subscriber_id, String status, String notification_title, String notification_description, String time_created) {
        this.streamer = streamer;
        this.subscriber = subscriber;
        this.streamer_id = streamer_id;
        this.subscriber_id = subscriber_id;
        this.status = status;
        this.notification_title = notification_title;
        this.notification_description = notification_description;
        this.time_created = time_created;
    }

    public String getTime_created() {
        return time_created;
    }

    public void setTime_created(String time_created) {
        this.time_created = time_created;
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

    public String getNotification_title() {
        return notification_title;
    }

    public void setNotification_title(String notification_title) {
        this.notification_title = notification_title;
    }

    public String getNotification_description() {
        return notification_description;
    }

    public void setNotification_description(String notification_description) {
        this.notification_description = notification_description;
    }
}
