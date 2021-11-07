import {Injector, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { TestPlotlyComponent } from './test-plotly/test-plotly.component';
import {HttpClientModule} from "@angular/common/http";
import {MatCardModule} from "@angular/material/card";
import {MatButtonModule} from "@angular/material/button";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MatGridListModule} from "@angular/material/grid-list";
import { GeneralComponent } from './components/general/general.component';
import {MatMenuModule} from "@angular/material/menu";
import { AboutComponent } from './components/about/about.component';
import {DialogErrorMessageComponent} from "./components/bases/messages/dialog-error-message/dialog-error-message.component";
import {DialogErrorsMessageComponent} from "./components/bases/messages/dialog-errors-message/dialog-errors-message.component";
import {MatDialogModule} from "@angular/material/dialog";
import {MatListModule} from "@angular/material/list";
import {BaseTableComponent} from "./components/bases/base-table/base-table.component";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatTableModule} from "@angular/material/table";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {MatOptionModule} from "@angular/material/core";
import {MatInputModule} from "@angular/material/input";
import { MapComponent } from './components/map/map.component';
import { DimComponent } from './components/dim/dim.component';


export let InjectorInstance: Injector;

@NgModule({
  declarations: [
    AppComponent, DialogErrorMessageComponent, DialogErrorsMessageComponent,
    TestPlotlyComponent, BaseTableComponent,
    GeneralComponent,
    AboutComponent,
    MapComponent,
    DimComponent
  ],
  imports: [
    BrowserModule, HttpClientModule, BrowserAnimationsModule,
    AppRoutingModule, MatCardModule, MatButtonModule, MatGridListModule, MatMenuModule, MatDialogModule, MatListModule, MatProgressSpinnerModule, MatSidenavModule, MatFormFieldModule, MatTableModule, MatIconModule, MatSortModule, MatCheckboxModule, MatSelectModule, MatOptionModule, MatInputModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
