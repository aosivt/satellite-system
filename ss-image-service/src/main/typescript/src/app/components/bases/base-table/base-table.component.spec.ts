import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {BaseTableComponent} from './base-table.component';

describe('BaseTableComponent', () => {
  let component: BaseTableComponent<any>;
  let fixture: ComponentFixture<BaseTableComponent<any>>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BaseTableComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BaseTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
