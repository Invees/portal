package de.invees.portal.processing.worker.service.controller;

import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.service.command.Command;
import de.invees.portal.common.model.service.command.CommandResponse;

public interface UserServiceController {

  void create(Order order);

  CommandResponse execute(Command command);

}
