package de.invees.portal.processing.worker.service.provider.proxmox;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VirtualMachine {

  private final int vmid;
  private final String name;
  @Builder.Default
  private int cores = 1;
  @Builder.Default
  private int memory = 512;

}
