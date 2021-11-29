package de.invees.portal.event.event;

import de.invees.portal.common.utils.provider.Provider;
import de.invees.portal.event.Application;
import de.invees.portal.event.configuration.Configuration;
import lombok.Getter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EventServerProvider implements Provider {

  private final Server server;
  private final ServerConnector connector;
  @Getter
  private final List<EventHandler> handlers = new ArrayList<>();

  public EventServerProvider(Configuration configuration) {
    server = new Server();
    connector = new ServerConnector(server);
    connector.setPort(configuration.getPort());
    server.addConnector(connector);

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);

    JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
      wsContainer.setMaxTextMessageSize(65535);
      wsContainer.setIdleTimeout(Duration.ofHours(3));
      wsContainer.addMapping("/", EventHandler.class);
    });
    new Thread(() -> {
      try {
        server.start();
        server.join();
      } catch (Exception e) {
        Application.LOGGER.error("", e);
      }
    }).start();
  }

  public void append(EventHandler client) {
    this.handlers.add(client);
  }

  public void remove(EventHandler client) {
    this.handlers.remove(client);
  }

}
