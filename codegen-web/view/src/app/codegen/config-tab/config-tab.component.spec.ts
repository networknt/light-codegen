import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigTabComponent } from './config-tab.component';

describe('ConfigTabComponent', () => {
  let component: ConfigTabComponent;
  let fixture: ComponentFixture<ConfigTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
