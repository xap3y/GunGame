package eu.xap3y.gungame.model;

import lombok.Getter;

@Getter
public class LevelProgress {
    private int level;

    public LevelProgress(int level) {
        this.level = level;
    }

    public void increment() {
        level++;
    }

    public void decrement() {
        if (level > 0) level--;
    }
}