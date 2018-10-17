package helium.com.igloo.Models;

public class LectureModel {

    private String id;
    private String owner_id;
    private String title;
    private String description;
    private String password;
    private Boolean isPublic;
    private Boolean isAvailable;
    private Boolean isLive;
    private String time_created;
    private String session_id;
    private String archive_id;
    private String thumbnail;
    private String owner_name;

    public LectureModel(String id, String owner_id, String title, String description, String password, Boolean isPublic, Boolean isAvailable, Boolean isLive, String time_created, String session_id, String archive_id, String owner_name) {

        this.id = id;
        this.owner_id = owner_id;
        this.title = title;
        this.description = description;
        this.password = password;
        this.isPublic = isPublic;
        this.isAvailable = isAvailable;
        this.isLive = isLive;
        this.time_created = time_created;
        this.session_id = session_id;
        this.archive_id = archive_id;
        this.thumbnail = thumbnail;
        this.owner_name = owner_name;
    }

    public LectureModel() {
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getArchive_id() {
        return archive_id;
    }

    public void setArchive_id(String archive_id) {
        this.archive_id = archive_id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getTime_created(){
        return time_created;
    }

    public void setTime_created(String time_created){
        this.time_created = time_created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return owner_id;
    }

    public void setOwnerId(String ownerId) {
        this.owner_id = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public Boolean getLive() {
        return isLive;
    }

    public void setLive(Boolean live) {
        isLive = live;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }
}
