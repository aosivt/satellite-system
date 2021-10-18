import {Component, OnInit} from '@angular/core';
// @ts-ignore
import * as Plotly from 'plotly.js/dist/plotly.js';
// @ts-ignore
import {Config, Data, Layout} from 'plotly.js/dist/plotly.js';
import {range} from "rxjs";
import {HttpClient} from "@angular/common/http";
import Map from 'ol/Map';

import Tile from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import View from 'ol/View';

import Feature from "ol/Feature";
import proj4 from "proj4";
import Point from 'ol/geom/Point';
import VectorSource from "ol/source/Vector";
import VectorLayer from "ol/layer/Vector";
import {fromLonLat} from "ol/proj";
import ImageLayer from "ol/layer/Image";
import State from "ol/source/State";
import Static from "ol/source/ImageStatic";
import {PlotlyHTMLElement} from "plotly.js";
import RasterSource from 'ol/source/Raster';
import {ScaleLine} from "ol/control";
import {Fill, Stroke, Style} from "ol/style";
import CircleStyle from "ol/style/Circle";
import {LinearRing, MultiPoint, Polygon} from "ol/geom";
import {GeoJSON} from "ol/format";
import WebGLPointsLayer from "ol/layer/WebGLPoints";

// Transformation from image coordinate space to georeferenced coordinate space:
// X_geo = GT(0) + X_pixel * GT(1) + Y_line * GT(2)
// Y_geo = GT(3) + X_pixel * GT(4) + Y_line * GT(5)'

const geojsonObject = {
  'type': 'FeatureCollection',
  'crs': {
    'type': 'name',
    'properties': {
      'name': 'EPSG:3857',
    },
  },
  'features': [
    {
      'type': 'Feature',
      'geometry': {
        'type': 'Polygon',
        'coordinates': [
          [
            [-5e6, 6e6],
            [-5e6, 8e6],
            [-3e6, 8e6],
            [-3e6, 6e6],
            [-5e6, 6e6],
          ],
        ],
      },
    },
    {
      'type': 'Feature',
      'geometry': {
        'type': 'Polygon',
        'coordinates': [
          [
            [-2e6, 6e6],
            [-2e6, 8e6],
            [0, 8e6],
            [0, 6e6],
            [-2e6, 6e6],
          ],
        ],
      },
    },
    {
      'type': 'Feature',
      'geometry': {
        'type': 'Polygon',
        'coordinates': [
          [
            [1e6, 6e6],
            [1e6, 8e6],
            [3e6, 8e6],
            [3e6, 6e6],
            [1e6, 6e6],
          ],
        ],
      },
    },
    {
      'type': 'Feature',
      'geometry': {
        'type': 'Polygon',
        'coordinates': [
          [
            [-2e6, -1e6],
            [-1e6, 1e6],
            [0, -1e6],
            [-2e6, -1e6],
          ],
        ],
      },
    },
  ],
};

const source = new VectorSource({
  features: new GeoJSON().readFeatures(geojsonObject),
});
const styles = [
  /* We are using two different styles for the polygons:
   *  - The first style is for the polygons themselves.
   *  - The second style is to draw the vertices of the polygons.
   *    In a custom `geometry` function the vertices of a polygon are
   *    returned as `MultiPoint` geometry, which will be used to render
   *    the style.
   */
  new Style({
    stroke: new Stroke({
      color: 'blue',
      width: 3,
    }),
    fill: new Fill({
      color: 'rgba(0, 0, 255, 0.1)',
    }),
  }),
  new Style({
    image: new CircleStyle({
      radius: 5,
      fill: new Fill({
        color: 'orange',
      }),
    }),
    geometry: function (feature) {
      // return the coordinates of the first ring of the polygon
      // @ts-ignore
      const coordinates = feature.getGeometry().getCoordinates()[0];
      return new MultiPoint(coordinates);
    },
  }),
];
const layer = new VectorLayer({
  source: source,
  style: styles,
});
@Component({
  selector: 'app-test-plotly',
  templateUrl: './test-plotly.component.html',
  styleUrls: ['./test-plotly.component.css']
})
export class TestPlotlyComponent implements OnInit {
  ws = new WebSocket("ws://localhost:8080/socket");

  callBack: Function = ()=>{};
  constructor(public http: HttpClient) { }

  public dataImage: string = '';
  public map: Map = new Map({
    controls: [new ScaleLine({
      units: 'metric',
    })]
  });

  private resultData: Result[] = [];
  private view = new View({
    center: [487648, 6015073],
    zoom: 5
  });

  ngOnInit(): void {
    const resultData: Result[] = [];
    this.ws.onopen = () => {
      this.ws.onmessage = (event) => {
        let obj: Result = JSON.parse(event.data);
        resultData.push(obj);
        if (resultData.length % 1000 === 0) {
          this.setDataZ(resultData.sort((r1, r2) => r1.rowId - r2.rowId).map(r => r.result));
        }
      }
    }
    this.fieldingPlotly();

    var projection = '+proj=utm +zone=45 +datum=WGS84 +units=m +no_defs'
    var templte = proj4(projection).inverse([487648, 6015073]);

    console.log(templte)
    // var tem = proj4x.default<number[]>(projection,[487648,6015073])
    // console.log(tem);
    // this.map
    this.map.setTarget('map');
    var osmLayer = new Tile({
      source: new OSM()
    });

    var vectorSource = new VectorSource();
    vectorSource.addFeature(new Feature(new Point([5e6, 7e6])));
    var satelliteLayer = new VectorLayer({
      source: vectorSource,
    })
    this.map.addLayer(osmLayer);
    this.map.addLayer(satelliteLayer);

    this.map.setView(this.view);

    this.map.on('click', function (evt) {
      console.log(evt.coordinate)
      console.log(fromLonLat(evt.coordinate))
      vectorSource.addFeature(new Feature(new Point(evt.coordinate)));
    });

    this.callBack = ((coord : number[])=>{
      console.log("this is callback")
      vectorSource.addFeature(new Feature(new Point(coord)));
    });

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
      // showscale: false,
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
    this.resultData = result;
    let z1:number[][] = result.sort((r1,r2)=>r2.rowId - r1.rowId).map(r=>r.result);
    let geoTransform = result[0].geoTransform;
    let projection = result[0].projection;
    let countPixel = result[0].result.length * result.length;
    this.setCoordinateView(result);
    this.viewSqOnOLFromResult(result);
    this.fieldOLMap(result);
    // this.map.setView(thiss.view);
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
      // showlegend: false,
      // showline: false,
      // showgrid: false,
      // showscale: false,
      type: 'heatmap',
      x: range(1, z1[0].length),
      y: range(-z1.length, 1),
      // autocolorscale: false,
      // colorbar: {
      //   visible: false,
      //   titleside: 'top'
        // ticklen: 4,
        // thickness: 20,
        // tickvals: [
        //   -20,
        //   -15,
        //   -10,
        //   -5,
        //   0
        // ]
      // },
    } ;

    this.setDivElementPlotLy(data_z1,geoTransform,projection,countPixel);
  }

  public setDivElementPlotLy(dataZ : any, geoTransform: number[],projection:string,countPixel:number) {
    const el = <PlotlyHTMLElement>document.getElementById('plotly');
    Plotly.newPlot(el, [dataZ], this.getLayoutForPlotLy(), {       displayModeBar: false,showAxisRangeEntryBoxes: false}).then((e: { on: (arg0: string, arg1: (event: any) => void) => void; })=>{
          e.on('plotly_click',(event => {
            let valueX = event.points[0].x!== undefined ? Number.isInteger(event.points[0].x)?Number.parseInt(<string>event.points[0].x)+1:0:0;
            let valueY = event.points[0].y!== undefined ? Number.isInteger(event.points[0].y)?Number.parseInt(<string>event.points[0].y)+1:0:0;
            let lengthZ = event.points[0].data.z.length;
            let X :number = valueX;
            let Y :number = lengthZ - valueY;
            let xCoord = geoTransform[0] + (X * geoTransform[1]) + (Y * geoTransform[2]);
            let yCoord = geoTransform[3] + (X * geoTransform[4]) + (Y * geoTransform[5]);

            //Координаты в UTM
            console.log(X, Y);
            console.log(xCoord, yCoord);

            console.log(geoTransform);

            //Площадь всей картинки
            console.log(countPixel*geoTransform[1]*geoTransform[5]);
            //Площадь пикселя
            console.log(geoTransform[1]*geoTransform[5]);


            console.log(projection);
            // const proj4 = (proj4x as any).default;
            let coord = proj4(projection).inverse([xCoord,yCoord]);
            // let coord = proj4('+proj=utm +zone=45n +datum=WGS84 +units=m +no_defs').inverse([xCoord,yCoord]);
            // proj4(projection).inverse([487648, 6015073]);
            console.log(coord);
            console.log([xCoord,yCoord]);
            this.callBack(fromLonLat(coord));
          }
        )
      );
      Plotly.toImage(e,{format: "jpeg", height:300,width:300})
        .then(
          (url: string) =>
          {
            this.dataImage = url;
          }
        ).then(()=>{

        // })
      })
    });
  }

  public setCoordinateView(result: Result[]){
    let latLong = this.getStartCoordinateForView(result);
    this.view.animate({
      center: latLong,
      duration: 2000,
      zoom: 10
    })
  }

  public getStartCoordinateForView(result: Result[]): number[]{
    let X :number = 0;
    let Y :number = result[0].result.length;
    return this.getCoordinateForView(X,Y,result);
  }
  public getCoordinateForView(X:number, Y:number,result: Result[]): number[]{
    return this.getCoordinateForViewFromGeoTransform(X,Y,result[0].geoTransform,result[0].projection);
  }
  public getCoordinateForViewFromGeoTransform(X:number, Y:number,geoTransform:number[],projection:string): number[]{
    let xCoord = geoTransform[0] + (X * geoTransform[1]) + (Y * geoTransform[2]);
    let yCoord = geoTransform[3] + (X * geoTransform[4]) + (Y * geoTransform[5]);
    let coord = proj4(projection).inverse([xCoord,yCoord]);
    return fromLonLat(coord);
  }

  public changeImage(): void {
    const imageLayer = new ImageLayer();
    const source = new Static({
      url: this.dataImage,
      projection: 'WGS:84',
      imageExtent: [0, 6013103, 490198, 6015063]
    //
    });
    imageLayer.setSource(source);
    this.map.addLayer(imageLayer);
  }


  public getLayoutForPlotLy(): any {
    const MIN_VAL = 0, MAX_VAL = 2;
    return {

      scene: {
        // yaxis:{visible:false},
        // xaxis:{visible:false},
        // yaxis:{visible:false}
        // axis: {
        //     nticks: 10,
        //     range: [ MIN_VAL, MAX_VAL ]
        // },
        // yaxis: {
        //     nticks: 10,
        //     range: [ MIN_VAL, MAX_VAL ]
        // },
        // zaxis:
        //   {
        // nticks: 7,
        // range: [ MIN_VAL, MAX_VAL ]
        // visible: false,
        // },
        // aspectmode: 'manual',
        // aspectratio: { x: 1, y: 1, z: 0.7 },
        // bgcolor : '#98ff6d'
      },
      showlegend: false,
      // xaxis: {visible: false},
      // yaxis: {visible: false},
      hidesources: true,
      legend: {x:0},
      autosize: true,
      width: 900,
      height: 900,
      margin: {l: 0, r: 0, b: 0, t: 10, pad: 20},

      // zoom: false
    } as Layout;
  }
  public convertTo3D(){
    const el = <PlotlyHTMLElement>document.getElementById('plotly');

    console.log(el)
    // @ts-ignore
    const dataEl = el['data'][0];
    dataEl.type = 'surface';
    const MIN_VAL = -25, MAX_VAL = 25;
    const layout = {
      scene: {
        zaxis: {
          nticks: 20,
          range: [ MIN_VAL, MAX_VAL ]
        },
      },
      autosize: false,
      width: 1024,
      height: 720,
      margin: { l: 0, r: 0, b: 50, t: 50, pad: 4 }
    };

    // @ts-ignore
    console.log(dataEl.z)
    const data = {
      z : dataEl.z ,
      // zmin: this.formPlot.minValueIndex, zmax: this.formPlot.maxValueIndex,
      type: 'surface',
      colorscale: dataEl.colorscale
    };

    // @ts-ignore
    Plotly.newPlot('plotly', [data], layout).then((e: { on: (arg0: string, arg1: (event: any) => void) => void; })=>{
      e.on('plotly_click',(event => {
            let valueX = event.points[0].x!== undefined ? Number.isInteger(event.points[0].x)?Number.parseInt(<string>event.points[0].x)+1:0:0;
            let valueY = event.points[0].y!== undefined ? Number.isInteger(event.points[0].y)?Number.parseInt(<string>event.points[0].y)+1:0:0;
            let lengthZ = event.points[0].data.z.length;
            let X :number = valueX;
            let Y :number = lengthZ - valueY;
            let xCoord = this.resultData[0].geoTransform[0] + (X * this.resultData[0].geoTransform[1]) + (Y * this.resultData[0].geoTransform[2]);
            let yCoord = this.resultData[0].geoTransform[3] + (X * this.resultData[0].geoTransform[4]) + (Y * this.resultData[0].geoTransform[5]);

            //Координаты в UTM
            // console.log(X, Y);
            // console.log(xCoord, yCoord);
            //
            // console.log(geoTransform);

            //Площадь всей картинки
            // console.log(countPixel*geoTransform[1]*geoTransform[5]);
            //Площадь пикселя
            // console.log(geoTransform[1]*geoTransform[5]);


            // console.log(projection);
            // const proj4 = (proj4x as any).default;
            let coord = proj4(this.resultData[0].projection).inverse([xCoord,yCoord]);
            // let coord = proj4('+proj=utm +zone=45n +datum=WGS84 +units=m +no_defs').inverse([xCoord,yCoord]);
            // proj4(projection).inverse([487648, 6015073]);
            console.log(coord);
            console.log([xCoord,yCoord]);
            this.callBack(fromLonLat(coord));
          }
        )
      )});
  }
  public fieldOLMap(result: Result[]){
    const vectorSource = new VectorSource();
    const vectorSource2 = new VectorSource();

    const myStyle = new Style({
      image: new CircleStyle({
        radius: 1,
        fill: new Fill({color: 'black'}),
        stroke: new Stroke({
          color: [255,0,0], width: 2
        })
      })
    })
    result.forEach((r, Y)=>{
        r.result.forEach((r2,X)=>{
          if (!isNaN(r2)){
            if (r2 > 0.15 && r2 < 0.3){
              const p = new Feature(new Point(this.getCoordinateForViewFromGeoTransform(X,result.length - Y,r.geoTransform,r.projection),{
                style: myStyle
              }));
              vectorSource.addFeature(p)
            }
            else if (r2 > 0.1 && r2 < 0.15){
              const p = new Feature(new Point(this.getCoordinateForViewFromGeoTransform(X,result.length - Y,r.geoTransform,r.projection),{
                style: myStyle
              }));
              vectorSource2.addFeature(p)
            }
          }
        });
    });
    // var pointsLayer = new VectorLayer({
    var pointsLayer =  new WebGLPointsLayer({
        source: vectorSource,
        style: {
          symbol: {
            symbolType: 'circle',
            size: 2,
            color: 'rgb(255, 0, 0)',
            opacity: 0.5,
          },
        },
      });
    var pointsLayer2 =  new WebGLPointsLayer({
      source: vectorSource2,
      style: {
        symbol: {
          symbolType: 'circle',
          size: 2,
          color: 'rgb(0,255,42)',
          opacity: 0.5,
        },
      },
    });
    this.map.addLayer(pointsLayer);
    this.map.addLayer(pointsLayer2);
  }
  public viewSqOnOLFromResult(result: Result[]){
    const vectorSource = new VectorSource();
    const Xmax = result[0].result.length;
    const Ymax = result.length;
    const p1 = this.getCoordinateForView(0,Ymax,result);
    const p2 = this.getCoordinateForView(0,0,result);
    const p3 = this.getCoordinateForView(Xmax,Ymax,result);
    const p4 = this.getCoordinateForView(Xmax,0,result);
    console.log(p1);
    const points: number[][][] = [[p1,p2,p4,p3,p1]]
    let geometry = new Polygon(points);
    vectorSource.addFeature(new Feature(geometry));
    var pointsLayer = new VectorLayer({
      source: vectorSource,
    })
    this.map.addLayer(pointsLayer);

  }

}
export interface Result{
  rowId: number;
  projection: string;
  geoTransform: number[];
  result: number[];
}

