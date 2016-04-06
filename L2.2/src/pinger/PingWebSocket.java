package pinger;

import base.AuthService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.istack.internal.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.jetbrains.annotations.NotNull;
import websocket.HandleException;
import websocket.Message;
import websocket.MessageHandlerContainer;

import java.io.IOException;

/**
 * Created by Solovyev.
 */
@WebSocket
public class PingWebSocket {
    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger(PingWebSocket.class);
    @NotNull
    private final AuthService authService;
    @NotNull
    private final PingService pingService;
    @NotNull
    private final MessageHandlerContainer messageHandlers;
    @NotNull
    private final String ownerSessionId;

    @Nullable
    private String owner;
    @Nullable
    private Session ownerSession;

    public PingWebSocket(@NotNull String sessionId, @NotNull AuthService authService, @NotNull PingService pingService, @NotNull MessageHandlerContainer messageHandlers) {
        this.authService = authService;
        this.pingService = pingService;
        this.messageHandlers = messageHandlers;
        this.ownerSessionId = sessionId;
    }

    @SuppressWarnings("unused")
    @OnWebSocketMessage
    public void onMessage(@NotNull Session session, @NotNull String text) {
        if (owner == null) {
            return;
        }


        final Message message;
        try {
            message = new Gson().fromJson(text, Message.class);
        } catch (JsonSyntaxException ex) {
            LOGGER.error("wrong json format at ping response", ex);
            return;
        }
        try {
            //noinspection ConstantConditions
            messageHandlers.handle(message, owner);
        } catch (HandleException e) {
            LOGGER.error("Can't handle message of type " + message.getType() + " with content: " + message.getContent(), e);
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketConnect
    public void onConnect(@NotNull Session session) {
        String userName = authService.getUserName(ownerSessionId);
        if (userName == null) {
            session.close(Response.SC_FORBIDDEN, "Your access to this resource is denied");
            return;
        }
        owner = userName;
        ownerSession = session;
        pingService.registerUser(userName, this);
        pingService.updatePing(userName);
    }

    public void sendMessage(@NotNull Message message) throws IOException {
        if (ownerSession == null || !ownerSession.isOpen()) {
            throw new IOException("session is closed or not exsists");
        }
        try {
            //noinspection ConstantConditions
            ownerSession.getRemote().sendString(new Gson().toJson(message));
        } catch (IOException e) {
            LOGGER.debug("error sending ping request", e);
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketClose
    public void onDisconnect(int statusCode, String reason) {
        LOGGER.info("User disconnected with code " + statusCode + " by reason: " + reason);
        if (owner != null) {
            pingService.unregisterUser(owner);
        }
    }
}
