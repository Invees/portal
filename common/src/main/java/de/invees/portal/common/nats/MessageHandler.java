package de.invees.portal.common.nats;

import de.invees.portal.common.nats.message.Message;

public interface MessageHandler {

  void handle(Message message);

}
