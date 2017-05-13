import {DEFAULT_DROPDOWN_CONFIG} from 'app/app.config';

export interface Schema {
	generatorType: string;
	uploadedSchemaFile: File;
}

export const GENERATORS_DROPDOWN_CONFIG: any = Object.assign({}, DEFAULT_DROPDOWN_CONFIG, {
	labelField: 'label',
	valueField: 'value',
	maxItems: 1
});