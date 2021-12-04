export default interface NetworkAddressV1 {
  _id: string;
  type: string;
  address: string;
  netmask: string;
  gateway: string;
  service: string;
}
