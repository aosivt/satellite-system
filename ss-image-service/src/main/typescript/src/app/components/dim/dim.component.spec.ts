import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DimComponent } from './dim.component';

describe('DimComponent', () => {
  let component: DimComponent;
  let fixture: ComponentFixture<DimComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DimComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DimComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
