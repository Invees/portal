import OneOffProductPriceV1 from "@/model/v1/product/price/OneOffProductPriceV1.ts";

export default interface ProductPriceV1 {
  amount: number;
  oneOffList: Array<OneOffProductPriceV1>;
}
