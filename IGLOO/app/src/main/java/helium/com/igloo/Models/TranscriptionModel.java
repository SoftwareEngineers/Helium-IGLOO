package helium.com.igloo.Models;

public class TranscriptionModel {
    private String word;
    private int time;
    private int position;

    public TranscriptionModel(String word, int time, int position) {
        this.word = word;
        this.time = time;
        this.position = position;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
