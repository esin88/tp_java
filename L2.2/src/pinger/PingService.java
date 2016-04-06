package pinger;

import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import websocket.Message;

import java.io.IOException;

/**
 * Created by Solovyev on 05/04/16.
 */
public interface PingService {
    void registerUser(@NotNull String userName, @NotNull PingWebSocket pingWebSocket);

    void unregisterUser(@NotNull String userName);

    void rememberPing(@NotNull String userName, long clientTimestamp, long requestId);

    void addListner(@NotNull PingListner pingListner);

    @Nullable
    TimingData getTimings(@NotNull String userName);

    void updatePing(@NotNull String userName);

    void updatePingAll();

    void shutdown();

    void sendMessageToUser(@NotNull String userName, @NotNull Message message) throws IOException;


}
