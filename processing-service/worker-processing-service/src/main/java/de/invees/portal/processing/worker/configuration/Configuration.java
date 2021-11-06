package de.invees.portal.processing.worker.configuration;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.configuration.NatsConfiguration;
import de.invees.portal.common.model.service.ServiceType;
import lombok.Data;

import java.util.UUID;

@Data
public class Configuration {

  private final UUID id;
  private final ServiceType serviceType;
  private final DataSourceConfiguration dataSource;
  private final NatsConfiguration nats;
  private final ProxmoxConfiguration proxmox;

}
