import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DialogErrorData} from "../dialog-error-message/data/dialog-error-data";

@Component({
  selector: 'app-dialog-errors-message',
  templateUrl: './dialog-errors-message.component.html',
  styleUrls: ['./dialog-errors-message.component.css']
})
export class DialogErrorsMessageComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<DialogErrorsMessageComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogErrorData[]) {
  }

  onNoClick(): void {
    close();
  }

  ngOnInit(): void {
  }

  public close(): void {
    this.dialogRef.close();
  }

  get title(): string{
    return  'Ошибка';
  }

}
