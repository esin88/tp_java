package pinger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import websocket.HandleException;
import websocket.Message;
import websocket.MessageHandler;
import websocket.MessageHandlerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Solovyev on 06/04/16.
 */
public class PingMessageHandlerContainer implements MessageHandlerContainer {
    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger(PingMessageHandlerContainer.class);
    final Map<Class<?>, MessageHandler<?>> handlerMap = new HashMap<>();

    @Override
    public void handle(@NotNull Message message, @NotNull String forUser) throws HandleException {

        final Class clazz;
        try {
            clazz = Class.forName(message.getType());
        } catch (ClassNotFoundException e) {
            throw new HandleException("Can't handle message of " + message.getType() + " type", e);
        }
        MessageHandler<?> messageHandler = handlerMap.get(clazz);
        if (messageHandler == null) {
            throw new HandleException("no handler for message of " + message.getType() + " type");
        }
        messageHandler.handleMessage(message, forUser);
        LOGGER.info("message handled: type =[" + message.getType() + "], content=[" + message.getContent() + ']');
    }

    @Override
    public <T> void registerHandler(@NotNull Class<T> clazz, MessageHandler<T> handler) {
        handlerMap.put(clazz, handler);
    }
}
