package helium.com.igloo.Models;

public class QuestionModel {
    private String owner_id;
    private String id;
    private String lecture;
    private String time;
    private String question;
    private long time_answered;
    private Boolean is_call;
    private Boolean is_answered;

    public QuestionModel() {
    }

    public QuestionModel(String owner, String id, String lecture, String time, String question, Boolean is_call, Boolean is_answered, long time_answered) {
        this.owner_id = owner;
        this.id = id;
        this.lecture = lecture;
        this.time = time;
        this.question = question;
        this.is_call = is_call;
        this.is_answered = is_answered;
        this.time_answered = time_answered;
    }

    public long getTime_answered() {
        return time_answered;
    }

    public void setTime_answered(long time_answered) {
        this.time_answered = time_answered;
    }

    public Boolean getIs_answered() {
        return is_answered;
    }

    public void setIs_answered(Boolean is_answered) {
        this.is_answered = is_answered;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public Boolean getIs_call() {
        return is_call;
    }

    public void setIs_call(Boolean is_call) {
        this.is_call = is_call;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLecture() {
        return lecture;
    }

    public void setLecture(String lecture) {
        this.lecture = lecture;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
