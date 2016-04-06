package pinger.handlers;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import pinger.PingService;
import pinger.TimingData;
import pinger.requests.GetPing;
import websocket.HandleException;
import websocket.Message;
import websocket.MessageHandler;

import java.io.IOException;

/**
 * Created by Solovyev on 06/04/16.
 */
public class GetPingHandler extends MessageHandler<GetPing.Request> {

    @NotNull
    private PingService pingService;

    public GetPingHandler(@NotNull PingService pingService) {
        super(GetPing.Request.class);
        this.pingService = pingService;
    }

    @Override
    public void handle(@NotNull GetPing.Request message, @NotNull String userName) throws HandleException {
        //noinspection ConstantConditions
        TimingData timings = pingService.getTimings(userName);

        GetPing.Response.Builder builder = GetPing.Response.create();
        if (timings != null) {
            builder
                    .ping(timings.getClientPing())
                    .clientTimeShift(timings.getClientTimeshift());

        }
        @SuppressWarnings("ConstantConditions")
        final Message response = new Message(GetPing.Response.class, new Gson().toJson(builder.build()));
        try {
            pingService.sendMessageToUser(userName, response);
        } catch (IOException e) {
            throw new HandleException("Unnable to send answer back to user "+ userName, e);
        }
    }
}
