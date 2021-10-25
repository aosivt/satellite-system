import {MatDialog} from "@angular/material/dialog";
import {InjectorInstance} from "../../../app.module";
import {DialogErrorMessageComponent} from "./dialog-error-message/dialog-error-message.component";
import {DialogErrorData} from "./dialog-error-message/data/dialog-error-data";
import {DialogErrorsMessageComponent} from "./dialog-errors-message/dialog-errors-message.component";

export class FabricMessage {

  constructor(public dialog: MatDialog, public editorDialog: MatDialog) {
  }

  static showDialogError(dialogErrorData: DialogErrorData) {
    const dialog = InjectorInstance.get<MatDialog>(MatDialog);
    dialog.open(DialogErrorMessageComponent, {
      width: '600px',
      height: '400px',
      data: dialogErrorData
    })
  }

  static showDialogErrors(dialogErrorsData: DialogErrorData[]) {
    const dialog = InjectorInstance.get<MatDialog>(MatDialog);
    dialog.open(DialogErrorsMessageComponent, {
      width: '600px',
      height: '400px',
      data: dialogErrorsData
    })
  }

}
