import DisplayV1 from "@/model/v1/DisplayV1.ts";
import OrderV1 from "@/model/v1/order/OrderV1.ts";

export default interface InvoicePositionV1 {
  displayName: DisplayV1;
  displayValue: DisplayV1;
  value: any;
  key: string;
  price: number;
  priceWithAddons: number;
  order: OrderV1;
  positionList: Array<InvoicePositionV1>;
  interval: number;
}
