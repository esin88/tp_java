package pinger.handlers;

import org.jetbrains.annotations.NotNull;
import pinger.PingService;
import pinger.requests.RefreshPing;
import websocket.HandleException;
import websocket.MessageHandler;

/**
 * Created by Solovyev on 06/04/16.
 */
public class RefreshPingHandler extends MessageHandler<RefreshPing.Request> {
@NotNull
    private PingService pingService;

    public RefreshPingHandler(@NotNull PingService pingService) {
        super(RefreshPing.Request.class);
        this.pingService = pingService;
    }

    @Override
    public void handle(@NotNull RefreshPing.Request message, @NotNull String userName) throws HandleException {
        pingService.updatePing(userName);
    }
}
