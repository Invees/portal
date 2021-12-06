package de.invees.portal.processing.worker.service.provider;

import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.service.command.CommandResponseV1;
import de.invees.portal.common.model.v1.service.command.CommandV1;
import de.invees.portal.common.model.v1.service.console.ServiceConsoleV1;
import de.invees.portal.common.utils.provider.Provider;
import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;

import java.util.List;
import java.util.UUID;

public interface ServiceProvider extends Provider {

  void create(ContractV1 contract);

  CommandResponseV1 execute(CommandV1 command);

  double getUsage();

  List<ServiceStatusV1> getAllServiceStatus();

  ServiceConsoleV1 createConsole(UUID service);

}
