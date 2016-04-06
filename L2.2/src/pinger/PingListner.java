package pinger;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Solovyev on 05/04/16.
 */
public interface PingListner {

    void notifyUserDisconnect(@NotNull String userName);
}
