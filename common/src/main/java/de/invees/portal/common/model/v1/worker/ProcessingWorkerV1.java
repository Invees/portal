package de.invees.portal.common.model.v1.worker;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProcessingWorkerV1 implements Model {

  private UUID id;
  private ServiceTypeV1 serviceType;

}
