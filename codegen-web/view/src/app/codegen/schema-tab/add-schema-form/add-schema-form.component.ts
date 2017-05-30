import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import {Schema} from 'app/codegen/schema-tab/schema.config';
import {FormGroup, FormBuilder} from '@angular/forms';
import {ADD_SCHEMA_FORM_GROUP, GENERATORS_DROPDOWN_CONFIG} from 'app/codegen/schema-tab/add-schema-form/add-schema-form.config';

@Component({
	selector: 'codegen-add-schema-form',
	templateUrl: './add-schema-form.component.html',
	styleUrls: ['./add-schema-form.component.css']
})
export class AddSchemaFormComponent implements OnInit {

	@Input() generatorTypes: {[item: string]: string};
	@Input() isLoadingGeneratorTypes: boolean;

	@Output() onAddSchema: EventEmitter<Schema> = new EventEmitter<Schema>(false);
	@Output() onFormChange: EventEmitter<FormGroup> = new EventEmitter<FormGroup>(false);

	selectedGenerator: string;
	generatorTypesDropdownConfig = GENERATORS_DROPDOWN_CONFIG;
	addSchemaForm: FormGroup;

	constructor(private formBuilder: FormBuilder) {}

	ngOnInit() {
		this.addSchemaForm = this.formBuilder.group(ADD_SCHEMA_FORM_GROUP);

		this.addSchemaForm.valueChanges.subscribe(() => {
			this.onFormChange.emit(this.addSchemaForm);
		});
	}

	// onAddSchemaClick(): void {
	// 	this.onAddSchema.emit({
	// 		generatorType: '',
	// 		uploadedSchemaFile: null
	// 	});
	// }

	onFileChangeClick($event): void {
		const schemaFile = $event.target.files[0];
		this.addSchemaForm.get('schemaFile').setValue(schemaFile);
	}
}
