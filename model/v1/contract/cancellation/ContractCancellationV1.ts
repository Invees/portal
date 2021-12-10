export default interface ContractCancellationV1 {
  _id: string;
  contract: number;
  createdAt: number;
  effectiveAt: number;
  cancel: boolean;
}
