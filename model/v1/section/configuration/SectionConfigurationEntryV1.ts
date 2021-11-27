import DisplayV1 from "@/model/v1/DisplayV1.ts";
import SectionConfigurationEntryOptionV1 from "@/model/v1/section/configuration/SectionConfigurationEntryOptionV1.ts";

export default interface SectionConfigurationEntryV1 {
  key: string;
  displayName: DisplayV1;
  optionList: Array<SectionConfigurationEntryOptionV1>;
}
