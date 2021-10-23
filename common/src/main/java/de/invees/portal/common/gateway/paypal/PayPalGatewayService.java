package de.invees.portal.common.gateway.paypal;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import de.invees.portal.common.configuration.PayPalConfiguration;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.utils.service.Service;

public class PayPalGatewayService implements Service {

  private final PayPalHttpClient client;
  public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0#");

  public PayPalGatewayService(PayPalConfiguration configuration) {
    this.client = new PayPalHttpClient(new PayPalEnvironment(
        configuration.getClientId(),
        configuration.getClientSecret(),
        configuration.getBaseUrl(),
        configuration.getWebUrl()
    ));
  }

  public HttpResponse<Order> validate(String orderId) throws IOException {
    OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
    request.requestBody(new OrderRequest());
    HttpResponse<Order> response = client.execute(request);
    return response;
  }

  public HttpResponse<Order> createOrder(Invoice invoice) throws IOException {
    OrdersCreateRequest request = new OrdersCreateRequest();

    OrderRequest orderRequest = new OrderRequest();
    orderRequest.checkoutPaymentIntent("CAPTURE");

    ApplicationContext applicationContext = new ApplicationContext().brandName("INVEES UG");
    orderRequest.applicationContext(applicationContext);

    List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
    PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest();
    purchaseUnitRequest.referenceId(invoice.getId() + "");
    purchaseUnitRequest.invoiceId(invoice.getId() + "");

    purchaseUnitRequest.amountWithBreakdown(
        new AmountWithBreakdown()
            .currencyCode("EUR")
            .value(format(invoice.getPrice().getAmount()))
            .amountBreakdown(new AmountBreakdown()
                .itemTotal(new Money().currencyCode("EUR").value(format(invoice.getPrice().getRaw())))
                .taxTotal(new Money().currencyCode("EUR").value(format(invoice.getPrice().getTaxes())))
            )
    );

    purchaseUnitRequests.add(purchaseUnitRequest);
    orderRequest.purchaseUnits(purchaseUnitRequests);

    request.requestBody(orderRequest);
    return this.client.execute(request);
  }

  private static final String format(double num) {
    return DECIMAL_FORMAT.format(num).replace(",", ".");
  }


}
