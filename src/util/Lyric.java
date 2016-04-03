package util;

import java.util.ArrayList;

public class Lyric {
    private String title;
    public ArrayList<LyricBlock> blocks;

    public Lyric(String title) {
        this.title = title;
        blocks = new ArrayList<>();
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}