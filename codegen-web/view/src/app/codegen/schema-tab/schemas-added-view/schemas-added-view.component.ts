import {Component, OnInit, Input} from '@angular/core';
import {Schema} from 'app/codegen/schema-tab/schema.config';

@Component({
	selector: 'codegen-schemas-added-view',
	templateUrl: './schemas-added-view.component.html',
	styleUrls: ['./schemas-added-view.component.css']
})
export class SchemasAddedViewComponent implements OnInit {

	@Input() addedSchemas: Schema[];

	constructor() {
	}

	ngOnInit() {
	}

}
