/**
 * Created by Nicholas Azar on 5/13/2017.
 */

import {Validators} from '@angular/forms';
import {DEFAULT_DROPDOWN_CONFIG} from 'app/app.config';

export const ADD_SCHEMA_FORM_GROUP = {
	generatorDropdown: [null, Validators.required],
	schemaFile: [null, Validators.required]
};

export const GENERATORS_DROPDOWN_CONFIG: any = Object.assign({}, DEFAULT_DROPDOWN_CONFIG, {
	labelField: 'label',
	valueField: 'value',
	maxItems: 1
});