package de.invees.portal.processing.master.configuration;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.configuration.NatsConfiguration;
import de.invees.portal.common.configuration.PayPalConfiguration;
import lombok.Data;

@Data
public class Configuration {

  private final DataSourceConfiguration dataSource;
  private final NatsConfiguration nats;

}
