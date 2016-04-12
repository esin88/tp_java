package base;

import org.jetbrains.annotations.NotNull;

/**
 * @author v.chibrikov
 */
public interface GameMechanics {

    public void addUser(@NotNull String user);

    public void incrementScore(@NotNull String userName);

    public void run();
}
