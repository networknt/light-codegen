/**
 * Created by Nicholas Azar on 5/11/2017.
 */

import {Action} from '@ngrx/store';
import {CodegenStage} from 'app/codegen/codegen.config';
import {createSelector} from 'reselect';
import {getCodegenState} from 'app/app.state';
import {type} from 'app/app.services';

export function codegen(state: CodegenState = CodegenActions.init, action: Action) {
	switch (action.type) {
		case CodegenActions.SETTING_CODEGEN_STAGE:
		case CodegenActions.FETCHING_GENERATOR_TYPES:
		case CodegenActions.FETCHED_GENERATOR_TYPES:
			return Object.assign({}, state, action.payload);
	}
}

export class CodegenActions {
	static init: CodegenState = {
		stage: CodegenStage.SCHEMA_DEFINITION,
		generatorTypes: {},
		isLoadingGeneratorTypes: false
	};

	static SETTING_CODEGEN_STAGE = type('[Codegen] Setting Codegen Stage');
	static FETCHING_GENERATOR_TYPES = type('[Codegen] Fetching Generator Types');
	static FETCHED_GENERATOR_TYPES = type('[Codegen] Fetched Generator Types');
	static FETCH_GENERATOR_TYPES_FAILED = type('[Codegen] Fetch Generator Types Failed');

	static setCodegenStage(stage: CodegenStage) {
		return {
			type: CodegenActions.SETTING_CODEGEN_STAGE,
			payload: {
				stage: stage
			}
		};
	}

	static fetchGeneratorTypes() {
		return {
			type: CodegenActions.FETCHING_GENERATOR_TYPES,
			payload: {
				generatorTypes: [],
				isLoadingGeneratorTypes: true
			}
		};
	}

	static fetchedGeneratorTypes(generatorTypes: {[item: string]: string}) {
		return {
			type: CodegenActions.FETCHED_GENERATOR_TYPES,
			payload: {
				generatorTypes: generatorTypes,
				isLoadingGeneratorTypes: false
			}
		};
	}

	static fetchGeneratorTypesFailed() {
		return {
			type: CodegenActions.FETCH_GENERATOR_TYPES_FAILED,
			payload: {
				generatorTypes: [],
				isLoadingGeneratorTypes: false
			}
		};
	}
}

export interface CodegenState {
	stage: CodegenStage;
	isLoadingGeneratorTypes: boolean;
	generatorTypes: {[item: string]: string};
}

export const getCodegenStage = createSelector(getCodegenState, (state: CodegenState) => state.stage);
export const getIsLoadingGeneratorTypes = createSelector(getCodegenState, (state: CodegenState) => state.isLoadingGeneratorTypes);
export const getGeneratorTypes = createSelector(getCodegenState, (state: CodegenState) => state.generatorTypes);
