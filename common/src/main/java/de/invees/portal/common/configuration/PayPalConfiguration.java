package de.invees.portal.common.configuration;

import lombok.Data;

@Data
public class PayPalConfiguration {

  private String clientId;
  private String clientSecret;
  private String baseUrl;
  private String webUrl;

}
