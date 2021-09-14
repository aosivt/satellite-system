import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestPlotlyComponent } from './test-plotly.component';

describe('TestPlotlyComponent', () => {
  let component: TestPlotlyComponent;
  let fixture: ComponentFixture<TestPlotlyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TestPlotlyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestPlotlyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
