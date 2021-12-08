import OrderV1 from "@/model/v1/order/OrderV1.ts";

export default interface ContractV1 {
  _id: number;
  belongsTo: string;
  type: string;
  createdAt: number;
  order: OrderV1;
  status: string;
  replacedWith: number;
}
