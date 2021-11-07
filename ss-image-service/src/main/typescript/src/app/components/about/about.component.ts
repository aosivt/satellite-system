import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DataAbout} from "../../types/data-about";

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<AboutComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DataAbout,
  ) { }

  ngOnInit(): void {
    console.log(this.data);
    // @ts-ignore
    console.log(this.data.data['data'].version);
  }

}
