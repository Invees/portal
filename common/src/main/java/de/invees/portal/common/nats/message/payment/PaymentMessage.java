package de.invees.portal.common.nats.message.payment;

import de.invees.portal.common.nats.message.Message;
import lombok.Data;

@Data
public class PaymentMessage implements Message {

  private final long invoiceId;

}
