package de.invees.portal.common.utils.invoice;

import com.itextpdf.html2pdf.HtmlConverter;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.*;
import de.invees.portal.common.exception.CalculationException;
import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.invoice.InvoiceFileV1;
import de.invees.portal.common.model.v1.invoice.InvoicePositionV1;
import de.invees.portal.common.model.v1.invoice.InvoiceStatusV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.invoice.price.InvoicePriceV1;
import de.invees.portal.common.model.v1.order.ContractUpgradeV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.product.price.IntervalProductPriceV1;
import de.invees.portal.common.model.v1.product.price.OneOffProductPriceV1;
import de.invees.portal.common.model.v1.section.SectionV1;
import de.invees.portal.common.model.v1.section.configuration.SectionConfigurationEntryOptionV1;
import de.invees.portal.common.model.v1.section.configuration.SectionConfigurationEntryV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.common.utils.template.TemplateUtils;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ContractUtils {

  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
  public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

  public static InvoiceV1 createByUpgrades(ContractV1 contract, List<ContractUpgradeV1> upgrades) {
    OrderV1 order = contract.getOrder();
    ProductV1 product = product(order.getProduct());
    SectionV1 section = section(product.getSection());
    long daysLeft = TimeUnit.MILLISECONDS.toDays(getNextPaymentDate(contract) - System.currentTimeMillis());

    System.out.println(daysLeft);

    List<InvoicePositionV1> positions = new ArrayList<>();
    double price = 0;

    for (ContractUpgradeV1 upgrade : upgrades) {
      price += calculateSubPosition(section, upgrade.getKey(), upgrade.getValue(), positions, daysLeft);
    }

    InvoiceV1 invoice = new InvoiceV1(
        invoiceDataSource().nextSequence(),
        contract.getBelongsTo(),
        new ArrayList<>(),
        new InvoicePriceV1(
            round(price) - taxes(round(price), 19),
            round(price),
            taxes(round(price), 19)
        ),
        System.currentTimeMillis(),
        -1,
        positions,
        InvoiceStatusV1.UNPAID
    );
    byte[] data = ContractUtils.generateInvoiceFile(invoice);

    invoiceFileDataSource().create(new InvoiceFileV1(
        invoice.getId(),
        data
    ));

    return invoice;
  }

  public static void applyUpgradeToContract(ContractV1 contract, List<ContractUpgradeV1> upgrades) {
    OrderV1 order = contract.getOrder();
    for (ContractUpgradeV1 upgrade : upgrades) {
      Object data = order.getConfiguration().get(upgrade.getKey());
      if (data instanceof Double) {
        order.getConfiguration().put(upgrade.getKey(), ((Double) data) + ((Number) upgrade.getValue()).doubleValue());
      } else if (data instanceof Integer) {
        order.getConfiguration().put(upgrade.getKey(), ((Integer) data) + ((Number) upgrade.getValue()).intValue());
      }
    }
    calculateOrder(order, new ArrayList<>()); // Validate
  }

  public static InvoiceV1 createByContracts(UUID userId, List<ContractV1> contracts) {
    return create(
        userId,
        contracts.stream()
            .map(contract -> contract.getOrder())
            .collect(Collectors.toList())
    );
  }

  public static InvoiceV1 create(UUID userId, List<OrderV1> order) {
    InvoiceV1 invoice = ContractUtils.calculate(invoiceDataSource().nextSequence(), userId, order);
    byte[] data = ContractUtils.generateInvoiceFile(invoice);

    invoiceFileDataSource().create(new InvoiceFileV1(
        invoice.getId(),
        data
    ));

    return invoice;
  }

  public static long getNextPaymentDate(ContractV1 contract) {
    List<InvoiceV1> invoices = invoiceDataSource()
        .getCollection()
        .find(Filters.eq(InvoiceV1.CONTRACT_LIST, contract.getId()))
        .map((d) -> invoiceDataSource().map(d, InvoiceV1.class))
        .into(new ArrayList<>());
    long paymentDate;
    if (invoices.size() == 1) {
      paymentDate = invoices.get(invoices.size() - 1).getPaidAt();
    } else {
      paymentDate = invoices.get(invoices.size() - 1).getCreatedAt();
    }
    return paymentDate + TimeUnit.DAYS.toMillis(contract.getOrder().getPaymentInterval()); // TODO: Change to days
  }

  public static InvoiceV1 calculate(long id, UUID userId, List<OrderV1> orders) {
    List<InvoicePositionV1> positions = new ArrayList<>();
    double price = 0;
    for (OrderV1 order : orders) {
      price += calculateOrder(order, positions);
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
        -1,
        positions,
        InvoiceStatusV1.UNPAID
    );
  }

  private static double calculateOrder(OrderV1 order, List<InvoicePositionV1> positions) {
    ProductV1 product = product(order.getProduct());
    SectionV1 section = section(product.getSection());
    double price = 0;

    for (String key : order.getConfiguration().keySet()) {
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
    for (SectionConfigurationEntryV1 entry : section.getConfigurationList()) {
      boolean found = false;
      for (String key : order.getConfiguration().keySet()) {
        if (entry.getKey().equals(key)) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new CalculationException("MISSING_CONFIGURATION_ENTRIES");
      }
    }
    for (Map.Entry<String, Object> entry : order.getConfiguration().entrySet()) {
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
    for (Map.Entry<String, Object> entry : order.getConfiguration().entrySet()) {
      price += calculateSubPosition(section, entry.getKey(), entry.getValue(), subPositions, order.getPaymentInterval());
    }

    OneOffProductPriceV1 oneOffPrice = getOneOffPrice(product, order.getContractTerm());
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
          new ArrayList<>(),
          order.getPaymentInterval(),
          0
      ));
    }
    price += oneOffPrice.getAmount();
    price += getIntervalPrice(product, order.getPaymentInterval()).getAmount();
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
        getIntervalPrice(product, order.getPaymentInterval()).getAmount(),
        price,
        order,
        subPositions,
        order.getPaymentInterval(),
        getIntervalPrice(product, 30).getAmount()
    ));
    return price;
  }

  private static double calculateSubPosition(SectionV1 section, String key, Object value, List<InvoicePositionV1> subPositions,
                                             long paymentDays) {
    subPositions.add(new InvoicePositionV1(
        getConfigurationEntry(section, key).getDisplayName(),
        getEntryOption(section, key, value).getDisplayValue(),
        value,
        key,
        (getEntryOption(section, key, value).getPrice()) * ((double) paymentDays / 30d),
        (getEntryOption(section, key, value).getPrice()) * ((double) paymentDays / 30d),
        null,
        new ArrayList<>(),
        (int) paymentDays,
        getEntryOption(section, key, value).getPrice()
    ));
    return (getEntryOption(section, key, value).getPrice()) * ((double) paymentDays / 30d);
  }

  private static IntervalProductPriceV1 getIntervalPrice(ProductV1 product, int paymentInterval) {
    for (IntervalProductPriceV1 intervalPrice : product.getPrice().getIntervalList()) {
      if (intervalPrice.getPaymentInterval() == paymentInterval) {
        return intervalPrice;
      }
    }
    return null;
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
      if (value instanceof Integer && option.getValue() instanceof Double
          && ((Integer) value).doubleValue() == (double) option.getValue()) {
        return option;
      }
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

  private static InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class)
        .access(InvoiceDataSourceV1.class);
  }

  private static InvoiceFileDataSourceV1 invoiceFileDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class)
        .access(InvoiceFileDataSourceV1.class);
  }

  public static double taxes(double amount, double percent) {
    return amount - (amount / (1 + (percent / 100)));
  }

  public static double round(double price) {
    return Math.round(price * 100.0) / 100.0;
  }

  public static byte[] generateInvoiceFile(InvoiceV1 invoice) {
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
          .replace("{{invoice_start}}", DATE_FORMAT.format(new Date(invoice.getCreatedAt())))
          .replace("{{invoice_payment_until}}", DATE_FORMAT.format(new Date(invoice.getCreatedAt() + (1000L * 60 * 60 * 24 * 30))))
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
        String paymentInterval = "";
        if (!position.getKey().equalsIgnoreCase("ONE_OFF")) {
          paymentInterval = position.getInterval() + " Tag(e)";
        }

        exportablePositions.add(positionTemplate
            .replace("{{item_position}}", positionIndex.get() + "")
            .replace("{{position_displayName}}", position.getDisplayValue().getDe())
            .replace("{{payment_interval}}", paymentInterval)
            .replace("{{product_price}}", DECIMAL_FORMAT.format(position.getPrice()))
            .replace("{{product_price_month}}", DECIMAL_FORMAT.format(position.getPricePerMonth()))
        );
        positionIndex.set(positionIndex.get() + 1);
      }
      exportablePositions.addAll(generateExportablePositions(position.getPositionList(), positionTemplate, positionIndex));
    }
    return exportablePositions;
  }

}
