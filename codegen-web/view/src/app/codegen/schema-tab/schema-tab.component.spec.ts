import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CodegenSchemaTabComponent} from './schema-tab.component';

describe('CodegenSchemaTabComponent', () => {
	let component: CodegenSchemaTabComponent;
	let fixture: ComponentFixture<CodegenSchemaTabComponent>;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [CodegenSchemaTabComponent]
		})
			.compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(CodegenSchemaTabComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
