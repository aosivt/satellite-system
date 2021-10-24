import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  title = 'typescript';
  ws = new WebSocket("ws://localhost:8080/socket");
  ngOnInit(): void {
    this.ws.onmessage = (event) => {
      console.log(event);
    }
    this.ws.onerror = (event) => {
      console.log(event);
    }
  }
  public send(): void{
    this.ws.send('{"data":"data"}');
  }


}
