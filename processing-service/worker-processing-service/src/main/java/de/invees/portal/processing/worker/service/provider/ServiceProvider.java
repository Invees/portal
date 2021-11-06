package de.invees.portal.processing.worker.service.provider;

import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.service.command.Command;
import de.invees.portal.common.model.service.command.CommandResponse;
import de.invees.portal.common.utils.provider.Provider;

public interface ServiceProvider extends Provider {

  void create(Order order);

  CommandResponse execute(Command command);

  double getUsage();

}
