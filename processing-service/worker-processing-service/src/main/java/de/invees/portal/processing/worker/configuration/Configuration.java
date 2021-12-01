package de.invees.portal.processing.worker.configuration;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.configuration.NatsConfiguration;
import lombok.Data;

import java.util.UUID;

@Data
public class Configuration {

  private final UUID id;
  private final String serviceType;
  private final String nfsStorage;
  private final String nfsDirectory;
  private final String diskPrefix;
  private final DataSourceConfiguration dataSource;
  private final NatsConfiguration nats;
  private final ProxmoxConfiguration proxmox;
  private final NetworkConfiguration network;

}
