import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogErrorsMessageComponent } from './dialog-errors-message.component';

describe('DialogErrorsMessageComponent', () => {
  let component: DialogErrorsMessageComponent;
  let fixture: ComponentFixture<DialogErrorsMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DialogErrorsMessageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DialogErrorsMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
