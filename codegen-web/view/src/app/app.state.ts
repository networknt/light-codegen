/**
 * Created by Nicholas Azar on 5/11/2017.
 */

import {CodegenState} from 'app/codegen/codegen.state';
import {SchemaState} from 'app/codegen/schema-tab/schema.state';

export interface AppState {
	codegen: CodegenState;
	schema: SchemaState;
}

export const getCodegenState = (state: AppState) => state.codegen;
export const getSchemaState = (state: AppState) => state.schema;
