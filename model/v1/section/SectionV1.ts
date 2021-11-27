import DisplayV1 from "@/model/v1/DisplayV1.ts";
import SectionFieldV1 from "@/model/v1/section/field/SectionFieldV1.ts";
import SectionConfigurationEntryV1 from "@/model/v1/section/configuration/SectionConfigurationEntryV1.ts";
import SectionTagV1 from "@/model/v1/section/tag/SectionTagV1.ts";

export default interface SectionV1 {
  _id: string;
  displayName: DisplayV1;
  description: DisplayV1;
  fieldList: Array<SectionFieldV1>;
  configurationList: Array<SectionConfigurationEntryV1>;
  tagList: Array<SectionTagV1>;
  active: boolean;
}
