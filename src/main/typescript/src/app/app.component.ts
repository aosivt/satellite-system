import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  title = 'typescript';
  ws = new WebSocket("ws://localhost:8080/socket", "protocolOne");
  ngOnInit(): void {
     this.ws.onopen = () => {
     this.ws.onmessage = (event) => {
        console.log(event);
  }



    };
  }
  public send(): void{
    this.ws.send("{\"data\":\"data\"}");
  }


}
