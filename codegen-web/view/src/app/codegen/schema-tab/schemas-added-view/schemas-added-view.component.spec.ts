import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SchemasAddedViewComponent } from './schemas-added-view.component';

describe('SchemasAddedViewComponent', () => {
  let component: SchemasAddedViewComponent;
  let fixture: ComponentFixture<SchemasAddedViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SchemasAddedViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SchemasAddedViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
