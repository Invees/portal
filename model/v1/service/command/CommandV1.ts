export default interface CommandV1 {
  _id: string;
  executor: string;
  service: string;
  action: string;
  data: any;
}
