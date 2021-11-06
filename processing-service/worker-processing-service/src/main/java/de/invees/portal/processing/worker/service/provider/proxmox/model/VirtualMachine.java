package de.invees.portal.processing.worker.service.provider.proxmox.model;

import lombok.Builder;
import lombok.Data;

@Data
public class VirtualMachine {

  private final int vmid;
  private final String name;
  private final int cpus;
  private final int maxmem;

}
