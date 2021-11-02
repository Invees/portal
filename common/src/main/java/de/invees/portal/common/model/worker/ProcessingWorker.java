package de.invees.portal.common.model.worker;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.service.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProcessingWorker implements Model {

  private UUID id;
  private ServiceType serviceType;

}
