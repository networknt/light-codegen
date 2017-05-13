import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddSchemaFormComponent } from './add-schema-form.component';

describe('AddSchemaFormComponent', () => {
  let component: AddSchemaFormComponent;
  let fixture: ComponentFixture<AddSchemaFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddSchemaFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddSchemaFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
