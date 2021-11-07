import { Component, OnInit } from '@angular/core';
import Map from "ol/Map";
import {ScaleLine} from "ol/control";
import Tile from "ol/layer/Tile";
import OSM from "ol/source/OSM";
import View from "ol/View";
import {SatelliteImageService} from "../../services/satellite-image-service";
import {SatelliteImage} from "../../types/db/dim/satellite-image";
import VectorSource from "ol/source/Vector";
import {Polygon} from "ol/geom";
import Feature from "ol/Feature";
import VectorLayer from "ol/layer/Vector";
import proj4 from "proj4";
import {fromLonLat} from "ol/proj";

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {

  public map: Map = new Map({
    controls: [new ScaleLine({
      units: 'metric',
    })]
  });
  private view = new View({
    center: [487648, 6015073],
    zoom: 5
  });

  constructor(public satelliteImageService:SatelliteImageService) { }

  ngOnInit(): void {
    this.initMap();
  }

  private initMap(): void{
    this.map.setTarget('map');
    let osmLayer = new Tile({
      source: new OSM()
    });
    this.map.addLayer(osmLayer);
    this.map.setView(this.view);

    this.satelliteImageService.getAll((response: SatelliteImage[])=>{
      response.forEach(r=>{
        const vectorSource = new VectorSource();
        const Xmax = 10980;
        const Ymax = 10980;
        const p1 = this.getCoordinateForViewFromGeoTransform(0,Ymax,r.geoTransform,r.projection);
        const p2 = this.getCoordinateForViewFromGeoTransform(0,0,r.geoTransform,r.projection);
        const p3 = this.getCoordinateForViewFromGeoTransform(Xmax,Ymax,r.geoTransform,r.projection);
        const p4 = this.getCoordinateForViewFromGeoTransform(Xmax,0,r.geoTransform,r.projection);
        const points: number[][][] = [[p1,p2,p4,p3,p1]]
        let geometry = new Polygon(points);
        vectorSource.addFeature(new Feature(geometry));
        var pointsLayer = new VectorLayer({
          source: vectorSource,
        })
        this.map.addLayer(pointsLayer);
      });

    });

  }
  public getCoordinateForViewFromGeoTransform(X:number, Y:number,geoTransform:number[],projection:string): number[]{
    let xCoord = geoTransform[0] + (X * geoTransform[1]) + (Y * geoTransform[2]);
    let yCoord = geoTransform[3] + (X * geoTransform[4]) + (Y * geoTransform[5]);
    let coord = proj4(projection).inverse([xCoord,yCoord]);
    return fromLonLat(coord);
  }
}
