package de.invees.portal.processing.worker.service.provider.proxmox.model;

import lombok.Data;

@Data
public class Storage {

  private final String storage;
  private final long total;
  private final long used;
  private final int enabled;
  private final int active;
  private final int avail;
  private final String content;

}
