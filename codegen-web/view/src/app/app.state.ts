/**
 * Created by Nicholas Azar on 5/11/2017.
 */

import {CodegenState} from 'app/codegen/codegen.state';
import {SchemaState} from 'app/codegen/schema-tab/schema.state';
import {ConfigState} from 'app/codegen/config-tab/config.state';

export interface AppState {
	codegen: CodegenState;
	schema: SchemaState;
	config: ConfigState;
}

export const getCodegenState = (state: AppState) => state.codegen;
export const getSchemaState = (state: AppState) => state.schema;
export const getConfigState = (state: AppState) => state.config;
