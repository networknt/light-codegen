/**
 * Created by Nicholas Azar on 5/11/2017.
 */

import {Action} from '@ngrx/store';
import {CodegenStage} from 'app/codegen/codegen.config';
import {createSelector} from 'reselect';
import {getCodegenState} from 'app/app.state';
import {type} from 'app/app.services';

export function codegen(state: CodegenState = CodegenActions.init, action: Action) {
	switch (action.type) {}
}

export class CodegenActions {
	static init: CodegenState = {
		stage: CodegenStage.SCHEMA_DEFINITION
	};

	static SET_CODEGEN_STAGE = type('[Codegen] Set Codegen Stage');
	static setCodegenStage(stage: CodegenStage) {
		return {
			type: CodegenActions.SET_CODEGEN_STAGE,
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