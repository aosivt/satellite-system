export enum ColumnsOptionsType {
  BOOLEAN_TYPE = "checkbox",
  STRING_TYPE = "text",
  LABEL_TYPE = "label",
  ENUM_TYPE = "select",
  ENTITY_TYPE = "select",
  BUTTON_TYPE = "button",
  DATE_TYPE = "date",
  DATETIME_TYPE = "datetime-local",
}

export class ColumnsOptions {
  key: string| undefined;
  indexPlace?: number;
  metaFieldName?: string;
  header: string| undefined;
  type?: ColumnsOptionsType = ColumnsOptionsType.STRING_TYPE;
  enum?: Function;
  enumKeys?: Function;
  enumValue?: Function;
  cell: Function | undefined;
  formatDate?: Function;
  showEditor?: Boolean = false;
  isEdit?: Boolean = false;
  functionEdit?: Function;
  rowEditing?: {};
  clickCell?: Function;
  select?: Function;
  inputType?: Function;
  rowSpan?: number = 1;
  colSpan?: number = 1;
  icon?: Function;
  actionHeader?: Function;
  menuId?: string;
}
