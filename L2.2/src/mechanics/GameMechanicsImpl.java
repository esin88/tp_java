package mechanics;

import base.GameMechanics;
import base.GameUser;
import base.WebSocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pinger.PingService;
import utils.TimeHelper;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author v.chibrikov
 */
public class GameMechanicsImpl implements GameMechanics {
    private static final long STEP_TIME = 100;
    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger(GameMechanicsImpl.class);

    private static final long GAME_TIME = TimeUnit.SECONDS.toMillis(15);
    @NotNull
    private final PingService pingService;

    @NotNull
    private WebSocketService webSocketService;

    @NotNull
    private Map<String, GameSession> nameToGame = new HashMap<>();

    @NotNull
    private Set<GameSession> allSessions = new HashSet<>();

    @Nullable
    private volatile String waiter;

    private long lastPingUpdate = 0;

    @NotNull
    private Clock clock = Clock.systemDefaultZone();

    @NotNull
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public GameMechanicsImpl(@NotNull WebSocketService webSocketService, @NotNull PingService pingService) {
        this.webSocketService = webSocketService;
        this.pingService = pingService;
    }

    @Override
    public void addUser(@NotNull String user) {
        tasks.add(()->addUserInternal(user));
    }

    private void addUserInternal(@NotNull String user) {
        if (waiter != null) {
            //noinspection ConstantConditions
            starGame(user, waiter);
            waiter = null;
        } else {
            waiter = user;
        }
    }

    @Override
    public void incrementScore(@NotNull String userName) {
        tasks.add(() -> incrementScoreInternal(userName));
    }

    private void incrementScoreInternal(String userName) {
        GameSession myGameSession = nameToGame.get(userName);
        GameUser myUser = myGameSession.getSelf(userName);
        myUser.incrementMyScore();
        GameUser enemyUser = myGameSession.getEnemy(userName);
        enemyUser.incrementEnemyScore();
        webSocketService.notifyMyNewScore(myUser);
        webSocketService.notifyEnemyNewScore(enemyUser);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        long lastFrameMillis = STEP_TIME;
        while (true) {
            final long before = clock.millis();
            gmStep(lastFrameMillis);
            updatePings(before);
            final long after = clock.millis();
            TimeHelper.sleep(STEP_TIME - (after - before));

            final long afterSleep = clock.millis();
            lastFrameMillis = afterSleep - before;
        }
    }

    private void gmStep(long frameTime) {
        while (!tasks.isEmpty()) {
            final Runnable nextTask = tasks.poll();
            if (nextTask != null) {
                try {
                    nextTask.run();
                } catch(RuntimeException ex) {
                  LOGGER.error("Cant handle game task", ex);
                }
            }

        }
        for (GameSession session : allSessions) {
            if (session.getSessionTime() > GAME_TIME) {
                boolean firstWin = session.isFirstWin();
                webSocketService.notifyGameOver(session.getFirst(), firstWin);
                webSocketService.notifyGameOver(session.getSecond(), !firstWin);
            }
        }
    }

    private void updatePings(long timestamp) {
        if (TimeUnit.MILLISECONDS.toSeconds(timestamp - lastPingUpdate) > 1) {
            lastPingUpdate = timestamp;
            pingService.refreshPingAll();
        }
    }

    private void starGame(@NotNull String first, @NotNull String second) {
        GameSession gameSession = new GameSession(first, second);
        allSessions.add(gameSession);
        nameToGame.put(first, gameSession);
        nameToGame.put(second, gameSession);

        webSocketService.notifyStartGame(gameSession.getSelf(first));
        webSocketService.notifyStartGame(gameSession.getSelf(second));
    }
}
