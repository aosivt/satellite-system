export class SatelliteImage {

  id: number = 0;
  fileId: number = 0;
  geoTransform: number[] = [];
  projection: string = '';
  name: string = '';
  satelliteId: number = 0;

  public static instance(): SatelliteImage{
    return {
      id: 0,
      fileId: 0,
      geoTransform: [],
      projection: '',
      name: '',
      satelliteId: 0
    }
  }
}
