import { Component, OnInit } from '@angular/core';
import Map from "ol/Map";
import {ScaleLine} from "ol/control";
import Tile from "ol/layer/Tile";
import OSM from "ol/source/OSM";
import View from "ol/View";

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

  constructor() { }

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
  }
}
