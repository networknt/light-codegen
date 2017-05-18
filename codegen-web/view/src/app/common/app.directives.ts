import {Directive, Input} from '@angular/core';
import {FormControl} from '@angular/forms';
/**
 * Created by Nicholas Azar on 5/17/2017.
 *
 * In reference to: https://github.com/angular/angular/issues/11271
 */

@Directive({
	selector: '[formControl][disableCond]'
})
export class DisableFormControlDirective {
	@Input() formControl: FormControl;

	constructor() {
	}

	get disableCond(): boolean { // getter, not needed, but here only to completude
		return !!this.formControl && this.formControl.disabled;
	}

	@Input('disableCond') set disableCond(s: boolean) {
		if (!this.formControl) {
			return;
		} else if (s) {
			this.formControl.disable();
		} else {
			this.formControl.enable();
		}
	}
}