package websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Solovyev on 06/04/16.
 */
public abstract class MessageHandler<T> {
    @NotNull
    private final Class<T> clazz;

    public MessageHandler(@NotNull Class<T> clazz) {
        this.clazz = clazz;
    }

    public void handleMessage(@NotNull Message message, @NotNull String forUser) throws HandleException {
        try {
            final Object data = new Gson().fromJson(message.getContent(), clazz);

            //noinspection ConstantConditions
            handle(clazz.cast(data), forUser);
        } catch (JsonSyntaxException | ClassCastException ex) {
            throw new HandleException("Can't read incoming message of type " + message.getType() + " with content: " + message.getContent(), ex);
        }
    }

    public abstract void handle(@NotNull T message, @NotNull String forUser) throws HandleException;
}
