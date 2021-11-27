import DisplayV1 from "@/model/v1/DisplayV1.ts";
import ProductPriceV1 from "@/model/v1/product/price/ProductPriceV1.ts";

export default interface ProductV1 {
  _id: string;
  section: string;
  displayName: DisplayV1;
  description: string;
  fieldList: any;
  type: string;
  price: ProductPriceV1;
  active: boolean;
}
