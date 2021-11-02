package de.invees.portal.processing.worker.configuration;

import lombok.Data;

@Data
public class ProxmoxConfiguration {

  private final String host;
  private final String username;
  private final String password;
  private final String realm;
  private final String node;

}
