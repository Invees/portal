package de.invees.portal.processing.worker.configuration;

import lombok.Data;

import java.util.List;

@Data
public class NetworkConfiguration {

  private final List<String> ipv4;
  private final List<String> ipv6;

}
