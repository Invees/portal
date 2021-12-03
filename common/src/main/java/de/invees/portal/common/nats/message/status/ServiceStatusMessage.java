package de.invees.portal.common.nats.message.status;

import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.nats.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ServiceStatusMessage implements Message {

  private List<ServiceStatusV1> status;

}
