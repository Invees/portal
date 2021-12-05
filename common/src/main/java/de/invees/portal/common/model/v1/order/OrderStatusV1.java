package de.invees.portal.common.model.v1.order;

/**
 * PAYMENT_REQUIRED > User is ordering via dashboard.
 * PROCESSING > User is paying the initialInvoice, service will be created now.
 * CANCELED > The order was canceled.
 * ACTIVE > Service was created and is now in usage.
 * EXTENDED > The user upgraded his service.
 * COMPLETED > The service is delivered and the user do not need. any service more "Kündigung".
 *
 */
public enum OrderStatusV1 {

  PAYMENT_REQUIRED, PROCESSING, ACTIVE, CANCELED, COMPLETED, EXTENDED

}
