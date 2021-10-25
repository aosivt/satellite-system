import { Component, OnInit } from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {AboutComponent} from "../about/about.component";
import {DataAbout} from "../../types/data-about";
import {Router} from "@angular/router";

@Component({
  selector: 'app-general',
  templateUrl: './general.component.html',
  styleUrls: ['./general.component.css']
})
export class GeneralComponent implements OnInit {

  constructor(public dialog: MatDialog,public router: Router) {

  }

  ngOnInit(): void {
  }

  public openAbout(){
    this.dialog.open(AboutComponent, {
      width: '650px',
      height: '650px',
      disableClose: true,
      hasBackdrop: false, // HERE
      panelClass: 'box-dialog',
      position: {
        right: 'calc(50vw - 304px)',
        top: 'calc(50vh - 334px)'
      },
      data: {
        data: {text: "Каркас", version: "0.0.1"}
      }
    });
  }
}
