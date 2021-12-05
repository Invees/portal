package de.invees.portal.invocing.configuration;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.configuration.NatsConfiguration;
import lombok.Data;

@Data
public class Configuration {

  private final DataSourceConfiguration dataSource;
  private final NatsConfiguration nats;

}
