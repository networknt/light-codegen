import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AddedSchemasComponent} from './added-schemas.component';

describe('AddedSchemasComponent', () => {
	let component: AddedSchemasComponent;
	let fixture: ComponentFixture<AddedSchemasComponent>;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [AddedSchemasComponent]
		})
			.compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(AddedSchemasComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
