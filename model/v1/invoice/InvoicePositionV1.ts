import DisplayV1 from "@/model/v1/DisplayV1.ts";
import OrderRequestV1 from "@/model/v1/order/request/OrderRequestV1.ts";

export default interface InvoicePositionV1 {
  displayName: DisplayV1;
  displayValue: DisplayV1;
  value: any;
  key: string;
  price: number;
  priceWithAddons: number;
  orderRequest: OrderRequestV1;
  positionList: Array<InvoicePositionV1>;
}
