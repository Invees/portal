package de.invees.portal.common.configuration;

import lombok.Data;

@Data
public class DataSourceConfiguration {

  private final String host;
  private final int port;
  private final String authDatabase;
  private final String user;
  private final String password;
  private final String database;
  private final int connectTimeout;
  private final int socketTimeOut;
  private final int maxConnectionIdleTime;
  private final int heartbeatFrequency;
  private final int maxWaitTime;

}
