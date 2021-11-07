import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {DialogErrorData} from "./data/dialog-error-data";

@Component({
  selector: 'app-dialog-error-message',
  templateUrl: './dialog-error-message.component.html',
  styleUrls: ['./dialog-error-message.component.css']
})
export class DialogErrorMessageComponent implements OnInit {

  @HostListener('window:beforeunload', ['$event']) unloadHandler(event: Event) {
    this.checkStatus();
  }

  constructor(
    public dialogRef: MatDialogRef<DialogErrorMessageComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogErrorData) {
  }

  onNoClick(): void {
    close();
  }

  ngOnInit(): void {
  }

  public close(): void {
    this.checkStatus();
    this.dialogRef.close();
  }



  private checkStatus(): void {
    console.log(this.data);
  }


}
