import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {DialogErrorMessageComponent} from './dialog-error-message.component';

describe('DialogErrorMessageComponent', () => {
  let component: DialogErrorMessageComponent;
  let fixture: ComponentFixture<DialogErrorMessageComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [DialogErrorMessageComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DialogErrorMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
