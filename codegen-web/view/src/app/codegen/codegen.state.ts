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
			return Object.assign({}, state, action.payload);
	}
	return state;
}

export class CodegenActions {
	static init: CodegenState = {
		stage: CodegenStage.SCHEMA_DEFINITION
	};
	static SETTING_CODEGEN_STAGE = type('[Codegen] Setting Codegen Stage');

	static setCodegenStage(stage: CodegenStage) {
		return {
			type: CodegenActions.SETTING_CODEGEN_STAGE,
			payload: {
				stage: stage
			}
		};
	}
}

export interface CodegenState {
	stage: CodegenStage;
}

export const getCodegenStage = createSelector(getCodegenState, (state: CodegenState) => state.stage);
