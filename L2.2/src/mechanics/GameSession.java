package mechanics;

import base.GameUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author v.chibrikov
 */
public class GameSession {
    private final long startTime;
    @NotNull
    private final GameUser first;
    @NotNull
    private final GameUser second;

    @NotNull
    private Map<String, GameUser> users = new HashMap<>();

    public GameSession(@NotNull String user1, @NotNull String user2) {
        startTime = Clock.systemDefaultZone().millis();
        GameUser gameUser1 = new GameUser(user1);
        gameUser1.setEnemyName(user2);

        GameUser gameUser2 = new GameUser(user2);
        gameUser2.setEnemyName(user1);

        users.put(user1, gameUser1);
        users.put(user2, gameUser2);

        this.first = gameUser1;
        this.second = gameUser2;
    }

    @Nullable
    public GameUser getEnemy(@NotNull String user) {
        @SuppressWarnings("ConstantConditions")
        String enemyName = users.containsKey(user) ? users.get(user).getEnemyName() : null;
        return enemyName == null ? null : users.get(enemyName);
    }

    @Nullable
    public GameUser getSelf(String user) {
        return users.get(user);
    }

    public long getSessionTime(){
        return Clock.systemDefaultZone().millis() - startTime;
    }

    @NotNull
    public GameUser getFirst() {
        return first;
    }

    @NotNull
    public GameUser getSecond() {
        return second;
    }

    public  boolean isFirstWin(){
        return first.getMyScore() > second.getMyScore();
    }
}
