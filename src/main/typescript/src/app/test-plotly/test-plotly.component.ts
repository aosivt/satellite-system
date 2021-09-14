import { Component, OnInit } from '@angular/core';
import * as Plotly from 'plotly.js';

import {Data, PlotlyHTMLElement} from "plotly.js";
import {range} from "rxjs";

@Component({
  selector: 'app-test-plotly',
  templateUrl: './test-plotly.component.html',
  styleUrls: ['./test-plotly.component.css']
})
export class TestPlotlyComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
    this.fieldingPlotly();
  }
  public fieldingPlotly() {

    const z1 = [[  -1, -0.6, 0, 1],
      [   1, -0.6, 0, 1],
      [-0.5, -0.6, 1, 0]];

    const data_z1 = {
      z: z1, type: 'heatmap', x: range(0, z1[0].length - 1), y: range(-z1.length + 1, 0),
      'autocolorscale': false,
      colorbar: {
        ticklen: 4,
        thickness: 20,
        tickvals: [
          -20,
          -15,
          -10,
          -5,
          0
        ]
      },
    } ;

    this.setDivElementPlotLy(data_z1);
  }
  public setDivElementPlotLy(dataZ : any) {
    const el = <PlotlyHTMLElement>document.getElementById('plotly');
    Plotly.newPlot(el, [dataZ], this.getLayoutForPlotLy(), { });
  }


  public getLayoutForPlotLy(): any {
    const MIN_VAL = 0, MAX_VAL = 2;
    const layout = {
      scene: {
        // axis: {
        //     nticks: 10,
        //     range: [ MIN_VAL, MAX_VAL ]
        // },
        // yaxis: {
        //     nticks: 10,
        //     range: [ MIN_VAL, MAX_VAL ]
        // },
        zaxis: {
          nticks: 7,
          range: [ MIN_VAL, MAX_VAL ]
        },
        // aspectmode: 'manual',
        // aspectratio: { x: 1, y: 1, z: 0.7 },
        // bgcolor : '#98ff6d'
      },
      autosize: true,
      width: 1200,
      height: 1200,
      margin: { l: 0, r: 0, b: 0, t: 10, pad: 20 },
      zoom: false
    };
    return layout;
  }
}
