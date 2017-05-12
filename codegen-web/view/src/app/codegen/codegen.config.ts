/**
 * Created by Nicholas Azar on 5/11/2017.
 */

import {DEFAULT_DROPDOWN_CONFIG} from 'app/app.config';

export enum CodegenStage {
	SCHEMA_DEFINITION,
	FEATURES_SELECTION,
	VERIFICATION
}

export const GENERATORS_DROPDOWN_CONFIG: any = Object.assign({}, DEFAULT_DROPDOWN_CONFIG, {
	labelField: 'item',
	valueField: 'item',
	searchField: 'item',
	placeholder: 'Click to select...',
	maxItems: 1
});