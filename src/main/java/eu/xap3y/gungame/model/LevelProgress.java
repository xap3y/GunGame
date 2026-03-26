package eu.xap3y.gungame.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public void decrement(int amount) {
        if (level > 0) level = Math.max(0, level - amount);
    }
}