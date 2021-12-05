export default interface PrototypeOrderV1 {
  _id: string;
  belongsTo: string;
  initialInvoice: number;
  orderTime: number;
  status: string;
  replacedWith: string;
}
