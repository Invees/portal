package de.invees.portal.processing.worker.service.provider.proxmox.model;

import lombok.Data;

@Data
public class Storage {

  private final String name;
  private final long total;
  private final long used;
  private final boolean enabled;
  private final boolean active;
  private final boolean avail;
  private final String content;

}
