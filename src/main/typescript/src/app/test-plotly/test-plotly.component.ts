import { Component, OnInit } from '@angular/core';
import * as Plotly from 'plotly.js';

import {Data, PlotlyHTMLElement} from "plotly.js";
import {range} from "rxjs";
import {HttpClient} from "@angular/common/http";
import proj from 'ol/proj';
import * as proj4x from 'proj4';



// Transformation from image coordinate space to georeferenced coordinate space:
// X_geo = GT(0) + X_pixel * GT(1) + Y_line * GT(2)
// Y_geo = GT(3) + X_pixel * GT(4) + Y_line * GT(5)

@Component({
  selector: 'app-test-plotly',
  templateUrl: './test-plotly.component.html',
  styleUrls: ['./test-plotly.component.css']
})
export class TestPlotlyComponent implements OnInit {
  ws = new WebSocket("ws://localhost:8080/socket");

  constructor(public http: HttpClient) { }

  ngOnInit(): void {
    const resultData: Result[] = [];
    this.ws.onopen = () => {
      this.ws.onmessage = (event) => {
        let obj: Result = JSON.parse(event.data);
        resultData.push(obj);
        if (resultData.length % 1000 === 0){
          this.setDataZ(resultData.sort((r1,r2)=>r1.rowId - r2.rowId).map(r=>r.result));
        }
      }
    }
    this.fieldingPlotly();
  }

  clickForField(){
    this.http.get("http://localhost:8080/api/get",{}).subscribe(r=>{
      let result: Result[] = r as Result[];
      this.setDataZModify(result);
      // this.setDataZ(result.sort((r1,r2)=>r1.rowId - r2.rowId).map(r=>r.result));
    })
  }

  public fieldingPlotly() {

    const z1 = [[  -1, -0.6, 0, 1],
      [   1, -0.6, 0, 1],
      [-0.5, -0.6, 1, 0]];

    this.setDataZ(z1)
  }
  public setDataZ(z1: number[][]) {

    const data_z1 = {
      z: z1,
      type: 'heatmap',
      x: range(0, z1[0].length - 1),
      y: range(-z1.length + 1, 0),
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

    this.setDivElementPlotLy(data_z1,[1,1,1,1,1,1],'',0);
  }
  public setDataZModify(result: Result[]) {

    let z1:number[][] = result.sort((r1,r2)=>r2.rowId - r1.rowId).map(r=>r.result);
    let geoTransform = result[0].geoTransform
    let projection = result[0].projection
    let countPixel = result[0].result.length * result.length
    // let x:number[] = [];
    // let y:number[] = [];

    // result.sort((r1,r2)=>r1.rowId - r2.rowId).forEach(r=>{
    //   z1.push(r.result);
    //   X_geo = GT(0) + X_pixel * GT(1) + Y_line * GT(2)
    //   Y_geo = GT(3) + X_pixel * GT(4) + Y_line * GT(5)
      // x = r.result.map((v,i)=> r.geoTransform[0] + (i * r.geoTransform[1]) + (r.rowId * r.geoTransform[2]));
      // y.push(r.geoTransform[3] + (r.rowId * r.geoTransform[4]) + (r.rowId * r.geoTransform[5]));
    // });

    const data_z1 = {
      z: z1,
      type: 'heatmap',
      x: range(1, z1[0].length),
      y: range(1, z1.length),
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

    this.setDivElementPlotLy(data_z1,geoTransform,projection,countPixel);
  }
  public setDivElementPlotLy(dataZ : any, geoTransform: number[],projection:string,countPixel:number) {
    const el = <PlotlyHTMLElement>document.getElementById('plotly');
    Plotly.newPlot(el, [dataZ], this.getLayoutForPlotLy(), { }).then(e=>{
          e.on('plotly_click',(event => {
            let valueX = event.points[0].x!== undefined ? Number.isInteger(event.points[0].x)?Number.parseInt(<string>event.points[0].x)+1:0:0;
            let valueY = event.points[0].y!== undefined ? Number.isInteger(event.points[0].y)?Number.parseInt(<string>event.points[0].y)+1:0:0;
            let X :number = valueX;
            let Y :number = valueY;
                //   X_geo = GT(0) + X_pixel * GT(1) + Y_line * GT(2)
                //   Y_geo = GT(3) + X_pixel * GT(4) + Y_line * GT(5)
            let xCoord = geoTransform[0] + (X * geoTransform[1]) + (Y * geoTransform[2]);
            let yCoord = geoTransform[3] + (X * geoTransform[4]) + (Y * geoTransform[5]);

            //Координаты в UTM
            console.log(xCoord, yCoord);

            console.log(geoTransform);

            //Площадь всей картинки
            console.log(countPixel*geoTransform[1]*geoTransform[5]);
            //Площадь пикселя
            console.log(geoTransform[1]*geoTransform[5]);


            console.log(projection);
            const proj4 = (proj4x as any).default;
            let coord = proj4(projection,[yCoord,xCoord]);
            console.log(coord);
          }
        )
      )
    });
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
export interface Result{
  rowId: number;
  projection: string;
  geoTransform: number[];
  result: number[];
}

