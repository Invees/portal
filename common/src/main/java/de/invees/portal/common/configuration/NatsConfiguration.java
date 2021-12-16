package de.invees.portal.common.configuration;

import lombok.Data;

@Data
public class NatsConfiguration {

  private String url;
  private String user;
  private String password;

}
