package de.invees.portal.common.model.v1.worker;

import de.invees.portal.common.model.v1.Model;
import de.invees.portal.common.model.v1.service.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProcessingWorker implements Model {

  private UUID id;
  private ServiceType serviceType;

}
