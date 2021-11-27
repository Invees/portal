package de.invees.portal.processing.worker.service.provider;

import de.invees.portal.common.model.v1.order.Order;
import de.invees.portal.common.model.v1.service.command.Command;
import de.invees.portal.common.model.v1.service.command.CommandResponse;
import de.invees.portal.common.model.v1.service.console.ServiceConsole;
import de.invees.portal.common.utils.provider.Provider;
import de.invees.portal.common.model.v1.service.status.ServiceStatus;

import java.util.List;
import java.util.UUID;

public interface ServiceProvider extends Provider {

  void create(Order order);

  CommandResponse execute(Command command);

  double getUsage();

  List<ServiceStatus> getAllServiceStatus();

  ServiceConsole createConsole(UUID service);

}
