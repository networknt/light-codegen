/**
 * Created by Nicholas Azar on 5/13/2017.
 */

import {Validators} from '@angular/forms';

export const ADD_SCHEMA_FORM_GROUP = {
	generatorDropdown: [null, Validators.required],
	schemaFile: [null, Validators.required]
};