import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {TestPlotlyComponent} from "./test-plotly/test-plotly.component";

const routes: Routes = [
  {path: 'plotly', component: TestPlotlyComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
