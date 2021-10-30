package de.invees.portal.processing.worker.service.controller.proxmox;

import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.service.command.Command;
import de.invees.portal.common.model.service.command.CommandResponse;
import de.invees.portal.common.model.service.command.ProxmoxAction;
import de.invees.portal.common.utils.process.ProcessUtils;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.service.controller.UserServiceController;

public class ProxmoxUserServiceController implements UserServiceController {

  @Override
  public void create(Order order) {
    try {
      ProcessUtils.exec("qm create 103", new String[]{
          "--cdrom", "nfs-iso:iso/debian-6.0.1-amd64-BD-1.iso",
          "--name", "squeeze-bd",
          "--vlan0", "virtio=62:57:BC:A2:0E:18",
      });
    } catch (Exception e) {
      Application.LOGGER.error("Error while creating order:", e);
    }
  }

  @Override
  public CommandResponse execute(Command command) {
    if (command.getAction().equals(ProxmoxAction.START)) {
      return _handleStart(command);
    }
    return new CommandResponse();
  }

  private CommandResponse _handleStart(Command command) {
    return null;
  }

}
