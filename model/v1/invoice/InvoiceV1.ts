import InvoicePriceV1 from "@/model/v1/invoice/price/InvoicePriceV1.ts";
import InvoicePositionV1 from "@/model/v1/invoice/InvoicePositionV1.ts";

export default interface InvoiceV1 {
  _id: number;
  belongsTo: string;
  contractList: Array<number>;
  price: InvoicePriceV1;
  createdAt: number;
  paidAt: number;
  positionList: Array<InvoicePositionV1>;
  status: string;
}
