package pinger.handlers;

import org.jetbrains.annotations.NotNull;
import pinger.PingService;
import pinger.requests.PingData;
import websocket.HandleException;
import websocket.MessageHandler;

/**
 * Created by Solovyev on 06/04/16.
 */
public class PingDataHandler extends MessageHandler<PingData.Response> {

    private PingService pingService;

    public PingDataHandler(@NotNull PingService pingService) {
        super(PingData.Response.class);
        this.pingService = pingService;
    }

    @Override
    public void handle(@NotNull PingData.Response message, @NotNull String userName) throws HandleException {
        //noinspection ConstantConditions
        pingService.rememberPing(userName, message.getTimestamp(), message.getId());
    }
}
