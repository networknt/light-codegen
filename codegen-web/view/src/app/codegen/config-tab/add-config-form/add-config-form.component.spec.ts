import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddConfigFormComponent } from './add-config-form.component';

describe('AddConfigFormComponent', () => {
  let component: AddConfigFormComponent;
  let fixture: ComponentFixture<AddConfigFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddConfigFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddConfigFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
