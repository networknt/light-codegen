/**
 * Created by Nicholas Azar on 5/11/2017.
 */

import {Action} from '@ngrx/store';
import {createSelector} from 'reselect';
import {getSchemaState} from 'app/app.state';
import {type} from 'app/app.services';
import {Schema} from 'app/codegen/schema-tab/schema.config';

export function schema(state: SchemaState = SchemaActions.init, action: Action) {
	switch (action.type) {
		case SchemaActions.FETCHING_GENERATOR_TYPES:
		case SchemaActions.FETCHED_GENERATOR_TYPES:
			return Object.assign({}, state, action.payload);
		case SchemaActions.ADD_SCHEMA:
			return Object.assign({}, state, {addedSchemas: [...state.addedSchemas, action.payload.schema]});
		case SchemaActions.REMOVE_SCHEMA:
			return Object.assign({}, state, {addedSchemas: [...state.addedSchemas.slice(0, action.payload.schemaIndex), ...state.addedSchemas.slice(action.payload.schemaIndex + 1)]});
		case SchemaActions.SELECT_SCHEMA:
			return Object.assign({}, state, {selectedSchema: state.addedSchemas[action.payload.schemaIndex]});
	}
	return state;
}

export class SchemaActions {
	static init: SchemaState = {
		generatorTypes: {},
		isLoadingGeneratorTypes: false,
		addedSchemas: [],
		selectedSchema: null
	};

	static FETCHING_GENERATOR_TYPES = type('[Schema] Fetching Generator Types');
	static FETCHED_GENERATOR_TYPES = type('[Schema] Fetched Generator Types');
	static FETCH_GENERATOR_TYPES_FAILED = type('[Schema] Fetch Generator Types Failed');
	static ADD_SCHEMA = type('[Schema] Add Schema');
	static REMOVE_SCHEMA = type('[Schema] Remove Schema');
	static SELECT_SCHEMA = type('[Schema] Select Schema');


	static fetchGeneratorTypes() {
		return {
			type: SchemaActions.FETCHING_GENERATOR_TYPES,
			payload: {
				generatorTypes: [],
				isLoadingGeneratorTypes: true
			}
		};
	}
	static fetchedGeneratorTypes(generatorTypes: {[item: string]: string}) {
		return {
			type: SchemaActions.FETCHED_GENERATOR_TYPES,
			payload: {
				generatorTypes: generatorTypes,
				isLoadingGeneratorTypes: false
			}
		};
	}
	static fetchGeneratorTypesFailed() {
		return {
			type: SchemaActions.FETCH_GENERATOR_TYPES_FAILED,
			payload: {
				generatorTypes: [],
				isLoadingGeneratorTypes: false
			}
		};
	}

	static addSchema(schema) {
		return {
			type: SchemaActions.ADD_SCHEMA,
			payload: {
				schema: schema
			}
		};
	}

	static removeSchema(schemaIndex) {
		return {
			type: SchemaActions.REMOVE_SCHEMA,
			payload: {
				schemaIndex: schemaIndex
			}
		};
	}

	static selectSchema(schemaIndex) {
		return {
			type: SchemaActions.SELECT_SCHEMA,
			payload: {
				schemaIndex: schemaIndex
			}
		};
	}
}

export interface SchemaState {
	isLoadingGeneratorTypes: boolean;
	generatorTypes: {[item: string]: string};
	addedSchemas: Schema[];
	selectedSchema: Schema;
}

export const getIsLoadingGeneratorTypes = createSelector(getSchemaState, (state: SchemaState) => state.isLoadingGeneratorTypes);
export const getGeneratorTypes = createSelector(getSchemaState, (state: SchemaState) => state.generatorTypes);
export const getAddedSchemas = createSelector(getSchemaState, (state: SchemaState) => state.addedSchemas);
export const getSelectedSchema = createSelector(getSchemaState, (state: SchemaState) => state.selectedSchema);
