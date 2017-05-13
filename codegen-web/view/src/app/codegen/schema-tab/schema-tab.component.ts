import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {SchemaService} from 'app/codegen/schema-tab/schema.service';
import {Observable} from 'rxjs/Observable';
import {Store} from '@ngrx/store';
import {AppState} from 'app/app.state';
import {getGeneratorTypes, getIsLoadingGeneratorTypes, getAddedSchemas} from 'app/codegen/schema-tab/schema.state';
import {Schema} from 'app/codegen/schema-tab/schema.config';
import {Form, FormGroup} from '@angular/forms';

@Component({
	selector: 'codegen-schema-tab',
	templateUrl: './schema-tab.component.html',
	styleUrls: ['./schema-tab.component.css']
})
export class CodegenSchemaTabComponent implements OnInit {

	generatorTypes: Observable<{[item: string]: string}>;
	isLoadingGeneratorTypes: Observable<boolean>;
	addedSchemas: Observable<Schema[]>;

	addSchemaForm: FormGroup;

	@Output() onContinue: EventEmitter<void> = new EventEmitter<void>(false);

	constructor(private store: Store<AppState>, private codegenService: SchemaService) {
		this.generatorTypes = this.store.select(getGeneratorTypes);
		this.isLoadingGeneratorTypes = this.store.select(getIsLoadingGeneratorTypes);
		this.addedSchemas = this.store.select(getAddedSchemas);
	}

	ngOnInit() {
		this.codegenService.getGeneratorTypes();
	}

	onAddAnotherClick() {
		this.codegenService.addSchema({
			generatorType: this.addSchemaForm.get('generatorDropdown').value,
			uploadedSchemaFile: this.addSchemaForm.get('schemaFile').value
		});
		this.addSchemaForm.reset();
	}

	onContinueClick() {
		if (this.addSchemaForm.valid) {
			this.codegenService.addSchema({
				generatorType: this.addSchemaForm.get('generatorDropdown').value,
				uploadedSchemaFile: this.addSchemaForm.get('schemaFile').value
			});
		}
		this.onContinue.emit();
	}

	onAddSchemaFormChange(addSchemaForm: FormGroup) {
		this.addSchemaForm = addSchemaForm;
	}

}
