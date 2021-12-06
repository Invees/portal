export default interface ServiceV1 {
  _id: string;
  name: string;
  belongsTo: string;
  contract: number;
  worker: string;
  type: string;
}
