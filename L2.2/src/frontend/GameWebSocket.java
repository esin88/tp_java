package frontend;

import base.GameMechanics;
import base.GameUser;
import base.WebSocketService;
import com.google.gson.JsonObject;
import com.sun.istack.internal.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@WebSocket
public class GameWebSocket {
    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger(GameWebSocket.class);

    @NotNull
    private String myName;
    @Nullable
    private Session session;
    @NotNull
    private GameMechanics gameMechanics;
    @NotNull
    private WebSocketService webSocketService;

    public GameWebSocket(@NotNull String myName, @NotNull GameMechanics gameMechanics, @NotNull WebSocketService webSocketService) {
        this.myName = myName;
        this.gameMechanics = gameMechanics;
        this.webSocketService = webSocketService;
    }

    @NotNull
    public String getMyName() {
        return myName;
    }

    public void startGame(@NotNull GameUser user) {
        try {
            final JsonObject jsonStart = new JsonObject();
            jsonStart.addProperty("status", "start");
            jsonStart.addProperty("enemyName", user.getEnemyName() == null ? "" : user.getEnemyName());
            if (session != null && session.isOpen())
                //noinspection ConstantConditions
                session.getRemote().sendString(jsonStart.toString());
        } catch (IOException | WebSocketException e) {
            LOGGER.error("Can't send web socket", e);
        }
    }

    public void gameOver(boolean win) {
        try {
            final JsonObject jsonEndGame = new JsonObject();
            jsonEndGame.addProperty("status", "finish");
            jsonEndGame.addProperty("win", win);
            if (session != null && session.isOpen())
                //noinspection ConstantConditions
                session.getRemote().sendString(jsonEndGame.toString());
        } catch (IOException | WebSocketException e) {
            LOGGER.error("Can't send web socket", e);
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketMessage
    public void onMessage(String data) {
        gameMechanics.incrementScore(myName);
    }

    @SuppressWarnings({"ParameterHidesMemberVariable", "unused"})
    @OnWebSocketConnect
    public void onOpen(@NotNull Session session) {
        this.session = session;
        webSocketService.addUser(this);
        gameMechanics.addUser(myName);
    }

    public void setMyScore(@NotNull GameUser user) {
        try {
            final JsonObject jsonEndGame = new JsonObject();
            jsonEndGame.addProperty("status", "increment");
            jsonEndGame.addProperty("name", myName);
            jsonEndGame.addProperty("score", user.getMyScore());
            if (session != null && session.isOpen())
                //noinspection ConstantConditions
                session.getRemote().sendString(jsonEndGame.toString());
        } catch (IOException | WebSocketException e) {
            LOGGER.error("Can't send web socket", e);
        }
    }

    public void setEnemyScore(@NotNull GameUser user) {
        if (user.getEnemyName() == null) {
            return;
        }
        try {
            final JsonObject jsonEndGame = new JsonObject();
            jsonEndGame.addProperty("status", "increment");
            jsonEndGame.addProperty("name", user.getEnemyName());
            jsonEndGame.addProperty("score", user.getEnemyScore());
            if (session != null && session.isOpen())
                //noinspection ConstantConditions
                session.getRemote().sendString(jsonEndGame.toString());
        } catch (IOException | WebSocketException e) {
            LOGGER.error("Can't send web socket", e);
        }
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        //...
    }
}
