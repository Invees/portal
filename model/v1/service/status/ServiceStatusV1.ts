export default interface ServiceStatusV1 {
  service: string;
  configuration: any;
  status: string;
  lastStart: number;
}
