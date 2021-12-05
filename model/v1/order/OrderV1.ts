import OrderRequestV1 from "@/model/v1/order/request/OrderRequestV1.ts";

export default interface OrderV1 {
  _id: string;
  belongsTo: string;
  initialInvoice: number;
  orderTime: number;
  request: OrderRequestV1;
  status: string;
  replacedWith: string;
}
