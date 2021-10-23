package de.invees.portal.common.utils.invoice;

import com.itextpdf.html2pdf.HtmlConverter;
import de.invees.portal.common.datasource.mongodb.UserDataSource;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.invoice.InvoiceStatus;
import de.invees.portal.common.model.price.Price;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.datasource.mongodb.SectionDataSource;
import de.invees.portal.common.exception.CalculationException;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.invoice.InvoicePosition;
import de.invees.portal.common.model.product.price.OneOffPrice;
import de.invees.portal.common.model.section.Section;
import de.invees.portal.common.model.section.configuration.SectionConfigurationEntry;
import de.invees.portal.common.model.section.configuration.SectionConfigurationEntryOption;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.utils.service.ServiceRegistry;
import de.invees.portal.common.utils.template.TemplateUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InvoiceUtils {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
  public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

  public static Invoice calculate(long id, UUID userId, List<OrderRequest> requests) {
    List<InvoicePosition> positions = new ArrayList<>();
    double price = 0;
    for (OrderRequest orderRequest : requests) {
      price += calculateOrderRequest(orderRequest, positions);
    }

    return new Invoice(
        id,
        userId,
        new Price(
            round(price) - taxes(round(price), 19),
            round(price),
            taxes(round(price), 19)
        ),
        System.currentTimeMillis(),
        positions,
        InvoiceStatus.UNPAID
    );
  }

  private static double calculateOrderRequest(OrderRequest orderRequest, List<InvoicePosition> positions) {
    Product product = product(orderRequest.getProductId());
    Section section = section(product.getSectionId());
    if (!product.isActive() || !section.isActive()) {
      throw new CalculationException("PRODUCT_INACTIVE");
    }
    OneOffPrice oneOffPrice = getOneOffPrice(product, orderRequest.getContractTerm());
    double price = 0;

    for (String key : orderRequest.getConfiguration().keySet()) {
      boolean found = false;
      for (SectionConfigurationEntry entry : section.getConfiguration()) {
        if (entry.getKey().equals(key)) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new CalculationException("TO_MANY_CONFIGURATION_ENTRIES");
      }
    }
    for (Map.Entry<String, Object> entry : orderRequest.getConfiguration().entrySet()) {
      boolean found = false;
      SectionConfigurationEntry configurationEntry = getConfigurationEntry(section, entry.getKey());
      for (SectionConfigurationEntryOption option : configurationEntry.getOptions()) {
        if (option.getValue().equals(entry.getValue())) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new CalculationException("INVALID_CONFIGURATION");
      }
    }

    List<InvoicePosition> subPositions = new ArrayList<>();
    for (Map.Entry<String, Object> entry : orderRequest.getConfiguration().entrySet()) {
      subPositions.add(new InvoicePosition(
          getConfigurationEntry(section, entry.getKey()).getDisplayName(),
          getEntryOption(section, entry.getKey(), entry.getValue()).getDisplayValue(),
          entry.getValue(),
          entry.getKey(),
          getEntryOption(section, entry.getKey(), entry.getValue()).getPrice(),
          getEntryOption(section, entry.getKey(), entry.getValue()).getPrice(),
          null,
          new ArrayList<>()
      ));
      price += getEntryOption(section, entry.getKey(), entry.getValue()).getPrice();
    }
    if (oneOffPrice.getAmount() != 0) {
      subPositions.add(new InvoicePosition(
          new Display(
              "Einrichtung",
              "Setup"
          ),
          new Display(
              "Einrichtung",
              "Setup"
          ),
          "setup-" + product.getId(),
          "ONE_OFF",
          oneOffPrice.getAmount(),
          oneOffPrice.getAmount(),
          null,
          new ArrayList<>()
      ));
    }
    price += oneOffPrice.getAmount();
    price += product.getPrice().getAmount();
    positions.add(new InvoicePosition(
        product.getDisplayName(),
        product.getDisplayName(),
        "product",
        product.getId(),
        product.getPrice().getAmount(),
        price,
        orderRequest,
        subPositions
    ));
    return price;
  }

  private static OneOffPrice getOneOffPrice(Product product, int contractTerm) {
    for (OneOffPrice oneOffPrice : product.getPrice().getOneOff()) {
      if (oneOffPrice.getContractTerm() == contractTerm) {
        return oneOffPrice;
      }
    }
    return null;
  }

  private static SectionConfigurationEntry getConfigurationEntry(Section section, String key) {
    for (SectionConfigurationEntry entry : section.getConfiguration()) {
      if (entry.getKey().equals(key)) {
        return entry;
      }
    }
    return null;
  }

  private static SectionConfigurationEntryOption getEntryOption(Section section, String key, Object value) {
    for (SectionConfigurationEntryOption option : getConfigurationEntry(section, key).getOptions()) {
      if (option.getValue().equals(value)) {
        return option;
      }
    }
    return null;
  }

  private static Section section(String id) {
    return ServiceRegistry.access(ConnectionService.class)
        .access(SectionDataSource.class)
        .byId(id, Section.class);
  }

  private static Product product(String id) {
    return ServiceRegistry.access(ConnectionService.class)
        .access(ProductDataSource.class)
        .byId(id, Product.class);
  }

  public static double taxes(double amount, double percent) {
    return amount - (amount / (1 + (percent / 100)));
  }

  public static double round(double price) {
    return Math.round(price * 100.0) / 100.0;
  }

  public static byte[] createInvoiceFile(Invoice invoice) {
    try {
      User user = ServiceRegistry.access(ConnectionService.class)
          .access(UserDataSource.class)
          .byId(invoice.getUserId().toString(), User.class);

      ByteArrayOutputStream invoiceBytes = new ByteArrayOutputStream();
      String invoiceTemplate = TemplateUtils.loadTemplate("invoice/de/index.html");
      String positionTemplate = TemplateUtils.loadTemplate("invoice/de/position.html");

      StringBuilder exportablePositions = new StringBuilder();
      for (String str : generateExportablePositions(invoice.getPositions(), positionTemplate, new AtomicInteger(1))) {
        exportablePositions.append(str);
      }

      String exportableInvoice = invoiceTemplate.replace("{{user_name}}", user.getName())
          .replace("{{invoice_start}}", DATE_FORMAT.format(new Date(invoice.getDate())))
          .replace("{{invoice_payment_until}}", DATE_FORMAT.format(new Date(invoice.getDate() + (1000L * 60 * 60 * 24 * 30))))
          .replace("{{user_company}}", user.getCompanyName())
          .replace("{{user_email}}", user.getEmail())
          .replace("{{user_phone}}", user.getPhone())
          .replace("{{user_city}}", user.getCity())
          .replace("{{user_firstName}}", user.getFirstName())
          .replace("{{user_lastName}}", user.getLastName())
          .replace("{{user_address}}", user.getAddress())
          .replace("{{user_postCode}}", user.getPostCode())
          .replace("{{user_country}}", user.getCountry())
          .replace("{{invoice_id}}", invoice.getId() + "")
          .replace("{{invoice_price_raw}}", DECIMAL_FORMAT.format(invoice.getPrice().getRaw()))
          .replace("{{invoice_price_taxes}}", DECIMAL_FORMAT.format(invoice.getPrice().getTaxes()))
          .replace("{{invoice_price}}", DECIMAL_FORMAT.format(invoice.getPrice().getAmount()))
          .replace("{{positions}}", exportablePositions);

      HtmlConverter.convertToPdf(
          exportableInvoice,
          invoiceBytes
      );
      HtmlConverter.convertToPdf(
          exportableInvoice,
          new FileOutputStream(new File("test.pdf"))
      );
      return invoiceBytes.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<String> generateExportablePositions(List<InvoicePosition> positions, String positionTemplate, AtomicInteger positionIndex) {
    List<String> exportablePositions = new ArrayList<>();
    for (InvoicePosition position : positions) {
      if (position.getPrice() != 0) {
        String months = "";
        if (!position.getKey().equalsIgnoreCase("ONE_OFF")) {
          months = "1 Monat(e)";
        }

        exportablePositions.add(positionTemplate
            .replace("{{item_position}}", positionIndex.get() + "")
            .replace("{{position_displayName}}", position.getDisplayValue().getDe())
            .replace("{{contract_time}}", months)
            .replace("{{product_price}}", DECIMAL_FORMAT.format(position.getPrice()))
        );
        positionIndex.set(positionIndex.get() + 1);
      }
      exportablePositions.addAll(generateExportablePositions(position.getPositions(), positionTemplate, positionIndex));
    }
    return exportablePositions;
  }

}