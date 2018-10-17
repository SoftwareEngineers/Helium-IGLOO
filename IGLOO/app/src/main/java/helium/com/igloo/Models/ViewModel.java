package helium.com.igloo.Models;

public class ViewModel {
    private Boolean viewed;

    public Boolean getViewed() {
        return viewed;
    }

    public void setViewed(Boolean viewed) {
        this.viewed = viewed;
    }

    public ViewModel(Boolean viewed) {

        this.viewed = viewed;
    }
}
