package de.invees.portal.core.utils.input;

import com.itextpdf.html2pdf.HtmlConverter;
import de.invees.portal.common.model.invoice.InvoiceStatus;
import de.invees.portal.common.model.price.Price;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.datasource.mongodb.SectionDataSource;
import de.invees.portal.common.exception.CalculationException;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.invoice.InvoiceConfigurationEntry;
import de.invees.portal.common.model.product.price.OneOffPrice;
import de.invees.portal.common.model.section.Section;
import de.invees.portal.common.model.section.configuration.SectionConfigurationEntry;
import de.invees.portal.common.model.section.configuration.SectionConfigurationEntryOption;
import de.invees.portal.common.utils.service.ServiceRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InvoiceUtils {

  public static Invoice calculate(int id, UUID userId, UUID orderId, Product product, Section section,
                                  Map<String, Object> configuration, int contractTerm, boolean ignoreOneOff) {
    for (String key : configuration.keySet()) {
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
    for (Map.Entry<String, Object> entry : configuration.entrySet()) {
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


    List<InvoiceConfigurationEntry> subEntries = new ArrayList<>();
    for (Map.Entry<String, Object> entry : configuration.entrySet()) {
      SectionConfigurationEntryOption option = getEntryOption(section, entry.getKey(), entry.getValue());
      subEntries.add(new InvoiceConfigurationEntry(entry.getKey(), round(option.getPrice())));
    }

    double price = 0;
    double oneOffPrice = 0;

    price += round(product.getPrice().getAmount());
    for (InvoiceConfigurationEntry subEntry : subEntries) {
      price += round(subEntry.getPrice());
    }
    for (OneOffPrice oneOffPriceItem : product.getPrice().getOneOff()) {
      if (oneOffPriceItem.getContractTerm() == contractTerm) {
        oneOffPrice = round(oneOffPriceItem.getAmount());
        break;
      }
    }

    if (ignoreOneOff) {
      oneOffPrice = 0;
    }

    return new Invoice(
        id,
        product.getId(),
        userId,
        orderId,
        round(product.getPrice().getAmount()),
        System.currentTimeMillis(),
        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30),
        new Price(
            round(price),
            round(taxes(price, 19))
        ),
        new Price(
            round(oneOffPrice),
            round(taxes(oneOffPrice, 19))
        ),
        subEntries,
        InvoiceStatus.UNPAID
    );
  }

  public static Invoice calculate(int id, UUID userId, UUID orderId, OrderRequest orderRequest) {
    ProductDataSource productDataSource = ServiceRegistry.access(ConnectionService.class).access(ProductDataSource.class);
    SectionDataSource sectionDataSource = ServiceRegistry.access(ConnectionService.class).access(SectionDataSource.class);

    Product product = productDataSource.byId(orderRequest.getProductId(), Product.class);
    if (product == null) {
      throw new CalculationException("INVALID_PRODUCT");
    }

    Section section = sectionDataSource.byId(product.getSectionId(), Section.class);

    if (section == null) {
      throw new CalculationException("INVALID_SECTION");
    }
    return calculate(
        id,
        userId,
        orderId,
        product,
        section,
        orderRequest.getConfiguration(),
        orderRequest.getContractTerm(),
        false
    );
  }

  public static double taxes(double amount, double percent) {
    return amount - (amount / (1 + (percent / 100)));
  }

  public static double round(double price) {
    return Math.round(price * 100.0) / 100.0;
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

  public static void createInvoiceFile() {
    try {
      HtmlConverter.convertToPdf(
          Files.readString(new File("C:\\Users\\kroseida\\Desktop\\rechnung\\test.html").toPath()),
          new FileOutputStream(new File("C:\\Users\\kroseida\\Desktop\\rechnung\\testx.pdf"))
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
