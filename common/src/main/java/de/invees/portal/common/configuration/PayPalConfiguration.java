package de.invees.portal.common.configuration;

import lombok.Data;

@Data
public class PayPalConfiguration {

  private final String clientId;
  private final String clientSecret;
  private final String baseUrl;
  private final String webUrl;

}
