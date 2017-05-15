import {Component, OnInit, Input, EventEmitter, Output} from '@angular/core';
import {Schema} from 'app/codegen/schema-tab/schema.config';
import {SchemaService} from 'app/codegen/schema-tab/schema.service';

@Component({
	selector: 'codegen-added-schemas',
	templateUrl: './added-schemas.component.html',
	styleUrls: ['./added-schemas.component.css']
})
export class AddedSchemasComponent implements OnInit {

	@Input() addedSchemas: Schema[];
	@Input() selectable = false;
	@Input() selectedSchemaIndex = 0;

	constructor(private schemaService: SchemaService) {}

	ngOnInit() {
		if (this.selectable) {
			this.schemaService.selectSchema(this.selectedSchemaIndex);
		}
	}

	onSchemaRemoveClick(schemaIndex: number) {
		this.schemaService.removeSchema(schemaIndex);
	}

	onSchemaSelectClick(schemaIndex: number) {
		if (this.selectable) {
			this.schemaService.selectSchema(schemaIndex);
		}
	}

}
