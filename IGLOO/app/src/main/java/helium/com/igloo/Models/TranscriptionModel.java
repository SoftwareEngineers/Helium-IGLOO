package helium.com.igloo.Models;

public class TranscriptionModel {
    private String word;
    private int time;
    private int position;

    public TranscriptionModel(String word, int time) {
        this.word = word;
        this.time = time;
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
}
