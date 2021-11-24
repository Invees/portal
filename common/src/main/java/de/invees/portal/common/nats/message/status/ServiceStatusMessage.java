package de.invees.portal.common.nats.message.status;

import de.invees.portal.common.model.service.status.ServiceStatus;
import de.invees.portal.common.nats.message.processing.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ServiceStatusMessage implements Message {

  private List<ServiceStatus> status;

}
