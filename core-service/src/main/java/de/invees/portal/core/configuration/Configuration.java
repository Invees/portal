package de.invees.portal.core.configuration;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import lombok.Data;

@Data
public class Configuration {

  private final DataSourceConfiguration dataSource;

}
