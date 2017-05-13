/**
 * Created by Nicholas Azar on 5/13/2017.
 */


import {SchemaActions} from 'app/codegen/schema-tab/schema.state';
import {CodegenApiService} from 'app/common/api/codegen-api.service';
import {Store} from '@ngrx/store';
import {AppState} from 'app/app.state';
import {Schema} from './schema.config';
import {Injectable} from '@angular/core';

@Injectable()
export class SchemaService {

	constructor(private store: Store<AppState>, private codegenApi: CodegenApiService) {}

	getGeneratorTypes(): void {
		this.store.dispatch(SchemaActions.fetchGeneratorTypes());
		this.codegenApi.getGeneratorTypes().subscribe((generatorTypes: {[item: string]: string}) => {
			this.store.dispatch(SchemaActions.fetchedGeneratorTypes(generatorTypes));
		}, (error) => {
			this.store.dispatch(SchemaActions.fetchGeneratorTypesFailed());
		});
	}

	addSchema(schema: Schema): void {
		this.store.dispatch(SchemaActions.addSchema(schema));
	}
}