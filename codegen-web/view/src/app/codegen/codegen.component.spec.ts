import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CodegenComponent } from './codegen.component';

describe('CodegenComponent', () => {
  let component: CodegenComponent;
  let fixture: ComponentFixture<CodegenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CodegenComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodegenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
