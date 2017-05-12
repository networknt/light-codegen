/**
 * Created by Nicholas Azar on 5/11/2017.
 */

import {CodegenState} from 'app/codegen/codegen.state';

export interface AppState {
	codegen: CodegenState;
}

export const getCodegenState = (state: AppState) => state.codegen;
