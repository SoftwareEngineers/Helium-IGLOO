package helium.com.igloo.Models;

public class TranscriptionModel {
    private String word;
    private double time;

    public TranscriptionModel(String word, double time) {
        this.word = word;
        this.time = time;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
