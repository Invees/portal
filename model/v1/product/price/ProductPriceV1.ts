import IntervalProductPriceV1 from "@/model/v1/product/price/IntervalProductPriceV1.ts";
import OneOffProductPriceV1 from "@/model/v1/product/price/OneOffProductPriceV1.ts";

export default interface ProductPriceV1 {
  intervalList: Array<IntervalProductPriceV1>;
  oneOffList: Array<OneOffProductPriceV1>;
}
