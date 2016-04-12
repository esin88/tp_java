package base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author v.chibrikov
 */
public class GameUser {
    @NotNull
    private final String myName;
    @Nullable
    private String enemyName;
    private int myScore = 0;
    private int enemyScore = 0;

    public GameUser(@NotNull String myName) {
        this.myName = myName;
    }

    @NotNull
    public String getMyName() {
        return myName;
    }
    @Nullable
    public String getEnemyName() {
        return enemyName;
    }

    public int getMyScore() {
        return myScore;
    }

    public int getEnemyScore() {
        return enemyScore;
    }

    public void incrementMyScore() {
        myScore++;
    }

    public void incrementEnemyScore() {
        enemyScore++;
    }

    public void setEnemyName(@NotNull String enemyName) {
        this.enemyName = enemyName;
    }
}
