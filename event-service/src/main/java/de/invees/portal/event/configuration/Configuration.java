package de.invees.portal.event.configuration;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.configuration.NatsConfiguration;
import lombok.Data;

@Data
public class Configuration {

  private final int port;
  private final String accessControlAllowOrigin;
  private final String accessControlAllowHeaders;
  private final DataSourceConfiguration dataSource;
  private final NatsConfiguration nats;

}
