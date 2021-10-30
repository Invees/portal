package de.invees.portal.processing.worker.configuration;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.configuration.NatsConfiguration;
import de.invees.portal.common.model.service.UserServiceType;
import lombok.Data;

import java.util.UUID;

@Data
public class Configuration {

  private final UUID id;
  private final UserServiceType serviceType;
  private final DataSourceConfiguration dataSource;
  private final NatsConfiguration nats;

}
