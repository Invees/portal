package de.invees.portal.common.model.v1.contract;

/**
 * PAYMENT_REQUIRED > User is contracting via dashboard.
 * PROCESSING > User is paying the initialInvoice, service will be created now.
 * CANCELED > The contract was canceled.
 * ACTIVE > Service was created and is now in usage.
 * EXTENDED > The user upgraded his service.
 * COMPLETED > The service is delivered and the user do not need. any service more "Kündigung".
 *
 */
public enum ContractStatusV1 {

  PAYMENT_REQUIRED, PROCESSING, ACTIVE, CANCELED, COMPLETED, EXTENDED

}
