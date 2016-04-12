package pinger;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pinger.requests.PingData;
import websocket.Message;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Solovyev on 05/04/16.
 */
public class PingServiceImpl implements PingService {
    private static final long CLEANER_PERIOD_SECONDS = TimeUnit.SECONDS.toMillis(10L);
    private static final long CLEANER_TRESHOLD_SECONDS = TimeUnit.SECONDS.toMillis(2L);
    private static final long MAX_PING_MILLIS = TimeUnit.SECONDS.toMillis(1L);

    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger(PingWebSocket.class);
    @NotNull
    private final Map<String, PingWebSocket> user2socket = new ConcurrentHashMap<>();
    @NotNull
    private final Queue<PingListner> listners = new ConcurrentLinkedQueue<>();
    @NotNull
    private final AtomicLong idGenerator = new AtomicLong(0);
    @NotNull
    private final Map<Long, Long> pendingRequests = new ConcurrentHashMap<>();
    @NotNull
    private final Map<String, TimingData> user2timings = new ConcurrentHashMap<>();
    @NotNull
    private final Executor pingUpdater = Executors.newSingleThreadExecutor();
    @NotNull
    private final ScheduledExecutorService cleanerExecutor = Executors.newScheduledThreadPool(1);

    @SuppressWarnings("FieldCanBeLocal")
    @NotNull
    private final Runnable cleaner = () -> {
        final long now = Clock.systemDefaultZone().millis();
        pendingRequests.entrySet().forEach(id2time -> {
            //noinspection ConstantConditions
            if (now - id2time.getValue() > CLEANER_TRESHOLD_SECONDS) {
                pendingRequests.remove(id2time.getKey());
            }

        });
    };

    public PingServiceImpl() {
        cleanerExecutor.scheduleAtFixedRate(cleaner, CLEANER_PERIOD_SECONDS, CLEANER_PERIOD_SECONDS, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean hasAlreadyRegistred(@NotNull String userName) {
        return user2socket.containsKey(userName);
    }

    @Override
    public void registerUser(@NotNull String userName, @NotNull PingWebSocket pingWebSocket) {
        //noinspection resource

        user2socket.put(userName, pingWebSocket);
    }

    @Override
    public void unregisterUser(@NotNull String userName) {
        @SuppressWarnings("resource") final boolean isRemoved = user2socket.remove(userName) != null;
        if (isRemoved) {
            listners.forEach(listner -> listner.notifyUserDisconnect(userName));
            user2timings.remove(userName);
        }

    }

    @Override
    public void rememberPing(@NotNull String userName, long clientTimestamp, long requestId) {
        final Long timeWas = pendingRequests.get(requestId);
        if (timeWas == null) {
            return;
        }
        final long now = Clock.systemDefaultZone().millis();
        final long ping = now - timeWas;
        if (ping > MAX_PING_MILLIS) {
            return;
        }
        final long clientTimeShift = now - (clientTimestamp + ping / 2);
        user2timings.put(userName, new TimingData(ping, clientTimeShift));
    }

    @Override
    public void addListner(@NotNull PingListner pingListner) {
        listners.add(pingListner);
    }

    @Nullable
    @Override
    public TimingData getTimings(@NotNull String userName) {
        return user2timings.get(userName);
    }

    @Override
    public void refreshPing(@NotNull String userName) {
        if (!user2socket.containsKey(userName)) {
            return;
        }
        pingUpdater.execute(() -> {
            final PingWebSocket pingWebSocket = user2socket.get(userName);
            if (pingWebSocket == null) {
                return;
            }
            final long id = idGenerator.getAndIncrement();
            final long now = Clock.systemDefaultZone().millis();
            final PingData.Request request = PingData.Request.create().id(id).build();
            @SuppressWarnings("ConstantConditions")
            final Message pingMessage = new Message(PingData.Request.class.getName(),
                    new Gson().toJson(request));
            try {
                pingWebSocket.sendMessage(pingMessage);
            } catch (IOException e) {
                LOGGER.error("Unnable to send ping request to user " + userName, e);
                return;
            }
            pendingRequests.put(id, now);
        });
    }

    @Override
    public void refreshPingAll() {
        user2socket.keySet().forEach(this::refreshPing);
    }

    @Override
    public void shutdown() {
        cleanerExecutor.shutdown();
    }

    @Override
    public void sendMessageToUser(@NotNull String userName, @NotNull Message message) throws IOException {
        final PingWebSocket pingWebSocket = user2socket.get(userName);
        if (pingWebSocket == null) {
            throw new IOException("no ping websocket for user " + userName);
        }
        pingWebSocket.sendMessage(message);
    }
}
