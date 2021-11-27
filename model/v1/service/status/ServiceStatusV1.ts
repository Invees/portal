import ServiceNetworkV1 from "@/model/v1/service/status/network/ServiceNetworkV1.ts";

export default interface ServiceStatusV1 {
  service: string;
  configuration: any;
  status: string;
  network: Array<ServiceNetworkV1>;
  uptime: number;
}
