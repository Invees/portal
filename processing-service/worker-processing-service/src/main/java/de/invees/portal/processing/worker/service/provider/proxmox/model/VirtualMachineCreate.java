package de.invees.portal.processing.worker.service.provider.proxmox.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VirtualMachineCreate {

  private final int vmid;
  private final String name;
  @Builder.Default
  private int cores = 1;
  @Builder.Default
  private int memory = 512;
  @Builder.Default
  private String sata0;
  @Builder.Default
  private String net0;
  @Builder.Default
  private int sockets = 1;
  @Builder.Default
  private String vga;

}
