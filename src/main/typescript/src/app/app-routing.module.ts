import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {TestPlotlyComponent} from "./test-plotly/test-plotly.component";
import {GeneralComponent} from "./components/general/general.component";
import {MapComponent} from "./components/map/map.component";

const routes: Routes = [
  {path: 'plotly', component: TestPlotlyComponent},
  {path: 'map', component: MapComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
