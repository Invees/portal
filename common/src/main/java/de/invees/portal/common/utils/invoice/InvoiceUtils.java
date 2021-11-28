package de.invees.portal.common.utils.invoice;

import com.itextpdf.html2pdf.HtmlConverter;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.SectionDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.UserDataSourceV1;
import de.invees.portal.common.exception.CalculationException;
import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.v1.invoice.InvoicePositionV1;
import de.invees.portal.common.model.v1.invoice.InvoiceStatusV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.invoice.price.InvoicePriceV1;
import de.invees.portal.common.model.v1.order.request.OrderRequestV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.product.price.OneOffProductPriceV1;
import de.invees.portal.common.model.v1.section.SectionV1;
import de.invees.portal.common.model.v1.section.configuration.SectionConfigurationEntryOptionV1;
import de.invees.portal.common.model.v1.section.configuration.SectionConfigurationEntryV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.common.utils.template.TemplateUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InvoiceUtils {

  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
  public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

  public static InvoiceV1 calculate(long id, UUID userId, List<OrderRequestV1> requests) {
    List<InvoicePositionV1> positions = new ArrayList<>();
    double price = 0;
    for (OrderRequestV1 orderRequest : requests) {
      price += calculateOrderRequest(orderRequest, positions);
    }

    return new InvoiceV1(
        id,
        userId,
        new ArrayList<>(),
        new InvoicePriceV1(
            round(price) - taxes(round(price), 19),
            round(price),
            taxes(round(price), 19)
        ),
        System.currentTimeMillis(),
        positions,
        InvoiceStatusV1.UNPAID
    );
  }

  private static double calculateOrderRequest(OrderRequestV1 orderRequest, List<InvoicePositionV1> positions) {
    ProductV1 product = product(orderRequest.getProduct());
    SectionV1 section = section(product.getSection());
    if (!product.isActive() || !section.isActive()) {
      throw new CalculationException("PRODUCT_INACTIVE");
    }
    double price = 0;

    for (String key : orderRequest.getConfiguration().keySet()) {
      boolean found = false;
      for (SectionConfigurationEntryV1 entry : section.getConfigurationList()) {
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
      SectionConfigurationEntryV1 configurationEntry = getConfigurationEntry(section, entry.getKey());
      for (SectionConfigurationEntryOptionV1 option : configurationEntry.getOptionList()) {
        if (option.getValue().equals(entry.getValue())) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new CalculationException("INVALID_CONFIGURATION");
      }
    }

    List<InvoicePositionV1> subPositions = new ArrayList<>();
    for (Map.Entry<String, Object> entry : orderRequest.getConfiguration().entrySet()) {
      subPositions.add(new InvoicePositionV1(
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

    OneOffProductPriceV1 oneOffPrice = getOneOffPrice(product, orderRequest.getContractTerm());
    if (oneOffPrice.getAmount() != 0) {
      subPositions.add(new InvoicePositionV1(
          new DisplayV1(
              "Einrichtung",
              "Setup"
          ),
          new DisplayV1(
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
    positions.add(new InvoicePositionV1(
        new DisplayV1(
            section.getDisplayName().getDe() + " " + product.getDisplayName().getDe(),
            section.getDisplayName().getEn() + " " + product.getDisplayName().getEn()
        ),
        new DisplayV1(
            section.getDisplayName().getDe() + " " + product.getDisplayName().getDe(),
            section.getDisplayName().getEn() + " " + product.getDisplayName().getEn()
        ),
        "product",
        product.getId(),
        product.getPrice().getAmount(),
        price,
        orderRequest,
        subPositions
    ));
    return price;
  }

  private static OneOffProductPriceV1 getOneOffPrice(ProductV1 product, int contractTerm) {
    for (OneOffProductPriceV1 oneOffPrice : product.getPrice().getOneOffList()) {
      if (oneOffPrice.getContractTerm() == contractTerm) {
        return oneOffPrice;
      }
    }
    return null;
  }

  private static SectionConfigurationEntryV1 getConfigurationEntry(SectionV1 section, String key) {
    for (SectionConfigurationEntryV1 entry : section.getConfigurationList()) {
      if (entry.getKey().equals(key)) {
        return entry;
      }
    }
    return null;
  }

  private static SectionConfigurationEntryOptionV1 getEntryOption(SectionV1 section, String key, Object value) {
    for (SectionConfigurationEntryOptionV1 option : getConfigurationEntry(section, key).getOptionList()) {
      if (option.getValue().equals(value)) {
        return option;
      }
    }
    return null;
  }

  private static SectionV1 section(String id) {
    return ProviderRegistry.access(DataSourceProvider.class)
        .access(SectionDataSourceV1.class)
        .byId(id, SectionV1.class);
  }

  private static ProductV1 product(String id) {
    return ProviderRegistry.access(DataSourceProvider.class)
        .access(ProductDataSourceV1.class)
        .byId(id, ProductV1.class);
  }

  public static double taxes(double amount, double percent) {
    return amount - (amount / (1 + (percent / 100)));
  }

  public static double round(double price) {
    return Math.round(price * 100.0) / 100.0;
  }

  public static byte[] createInvoiceFile(InvoiceV1 invoice) {
    try {
      UserV1 user = ProviderRegistry.access(DataSourceProvider.class)
          .access(UserDataSourceV1.class)
          .byId(invoice.getBelongsTo().toString(), UserV1.class);

      ByteArrayOutputStream invoiceBytes = new ByteArrayOutputStream();
      String invoiceTemplate = TemplateUtils.loadTemplate("invoice/de/index.html");
      String positionTemplate = TemplateUtils.loadTemplate("invoice/de/position.html");

      StringBuilder exportablePositions = new StringBuilder();
      for (String str : generateExportablePositions(invoice.getPositionList(), positionTemplate, new AtomicInteger(1))) {
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
      return invoiceBytes.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<String> generateExportablePositions(List<InvoicePositionV1> positions, String positionTemplate,
                                                         AtomicInteger positionIndex) {
    List<String> exportablePositions = new ArrayList<>();
    for (InvoicePositionV1 position : positions) {
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
      exportablePositions.addAll(generateExportablePositions(position.getPositionList(), positionTemplate, positionIndex));
    }
    return exportablePositions;
  }

}
