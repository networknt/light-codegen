/**
 * Created by Nicholas Azar on 5/14/2017.
 */

import {Action} from '@ngrx/store';
import {createSelector} from 'reselect';
import {type} from 'app/app.services';
import {getConfigState} from '../../app.state';

export function config(state: ConfigState = ConfigActions.init, action: Action) {
	switch (action.type) {
		case ConfigActions.FETCHING_SCHEMA:
		case ConfigActions.SCHEMA_FETCHED:
			return Object.assign({}, state, action.payload);
	}
	return state;
}

export class ConfigActions {
	static init: ConfigState = {
		isLoadingSchema: false,
		loadedSchema: null,
		schemaFormData: null
	};

	static FETCHING_SCHEMA = type('[Config] Fetching Schema');
	static SCHEMA_FETCHED = type('[Config] Schema Fetched');

	static fetchSchema() {
		return {
			type: ConfigActions.FETCHING_SCHEMA,
			payload: {
				isLoadingSchema: true,
				loadedSchema: null,
				schemaFormData: null
			}
		};
	}

	static schemaFetched(schema: any) {
		return {
			type: ConfigActions.SCHEMA_FETCHED,
			payload: {
				isLoadingSchema: false,
				loadedSchema: schema,
				schemaFormData: null
			}
		};
	}

}

export interface ConfigState {
	isLoadingSchema: boolean;
	loadedSchema: any;
	schemaFormData: any;
}

export const getIsLoadingSchema = createSelector(getConfigState, (state: ConfigState) => state.isLoadingSchema);
export const getLoadedSchema = createSelector(getConfigState, (state: ConfigState) => state.loadedSchema);
export const getSchemaFormData = createSelector(getConfigState, (state: ConfigState) => state.schemaFormData);
