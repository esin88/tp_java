package main;

import base.AuthService;
import base.GameMechanics;
import base.WebSocketService;
import chat.WebSocketChatServlet;
import frontend.AuthServiceImpl;
import frontend.GameServlet;
import frontend.WebSocketGameServlet;
import frontend.WebSocketServiceImpl;
import mechanics.GameMechanicsImpl;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import pinger.*;
import pinger.handlers.GetPingHandler;
import pinger.handlers.PingDataHandler;
import pinger.handlers.RefreshPingHandler;
import pinger.requests.GetPing;
import pinger.requests.PingData;
import pinger.requests.RefreshPing;
import websocket.MessageHandlerContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        final Server server = new Server(8080);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        final WebSocketService webSocketService = new WebSocketServiceImpl();
        final GameMechanics gameMechanics = new GameMechanicsImpl(webSocketService);
        final AuthService authService = new AuthServiceImpl();
        final PingService pingService = new PingServiceImpl();
        final MessageHandlerContainer pingMessageHandlerContainer = new PingMessageHandlerContainer();
        pingMessageHandlerContainer.registerHandler(GetPing.Request.class, new GetPingHandler(pingService));
        pingMessageHandlerContainer.registerHandler(PingData.Response.class, new PingDataHandler(pingService));
        pingMessageHandlerContainer.registerHandler(RefreshPing.Request.class, new RefreshPingHandler(pingService));

        //for chat example
        context.addServlet(new ServletHolder(new WebSocketChatServlet()), "/chat");

        //for game example
        context.addServlet(new ServletHolder(new WebSocketGameServlet(authService, gameMechanics, webSocketService)), "/gameplay");
        context.addServlet(new ServletHolder(new GameServlet(gameMechanics, authService)), "/game.html");

        context.addServlet(new ServletHolder(new WebSocketPingServlet(pingMessageHandlerContainer, pingService, authService )), "/ping");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase("static");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, context});
        server.setHandler(handlers);

        server.setHandler(handlers);

        server.start();

        //run GM in main thread
        gameMechanics.run();
    }
}
