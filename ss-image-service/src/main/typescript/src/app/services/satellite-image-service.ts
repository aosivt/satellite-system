import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {SatelliteImage} from "../types/db/dim/satellite-image";

//ts-ignore
@Injectable({providedIn: 'root'})
export class SatelliteImageService{

  private readonly URL_CLIENT_FILE_SERVICE = '/SatelliteImage';

  private readonly url: string = !environment.production ?
    'http://localhost:8080'.concat(this.URL_CLIENT_FILE_SERVICE): this.URL_CLIENT_FILE_SERVICE

  constructor(protected http: HttpClient) {

  }

  public getAll(callback:Function):void {
    this.http.get(this.url).subscribe((response)=>{
      callback(response as SatelliteImage)
    })
  }
}
