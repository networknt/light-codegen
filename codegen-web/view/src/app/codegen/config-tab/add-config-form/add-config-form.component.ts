import {Component, OnInit} from '@angular/core';
import {FormGroup, FormBuilder, FormControl} from '@angular/forms';

@Component({
	selector: 'add-config-form',
	templateUrl: './add-config-form.component.html',
	styleUrls: ['./add-config-form.component.css']
})
export class AddConfigFormComponent implements OnInit {

	configFromUploadFile: File;
	isConfigUrlDisabled = false;

	configForm: FormGroup;

	constructor(private formBuilder: FormBuilder) {
		this.configForm = this.formBuilder.group({
			configFile: new FormControl({value: null, disabled: this.isConfigUrlDisabled}),
			configUrl: new FormControl(null)
		});

		// this.configForm.get('configFile').valueChanges.subscribe((file: File) => {
		// 	if (file) {
		// 		this.configForm.get('configUrl').disable();
		// 	} else {
		// 		this.configForm.get('configUrl').enable();
		// 	}
		// 	this.configForm.updateValueAndValidity();
		// });
        //
		// this.configForm.get('configUrl').valueChanges.subscribe((configUrl: string) => {
		// 	if (configUrl) {
		// 		this.configForm.get('configFile').disable();
		// 	} else {
		// 		this.configForm.get('configFile').enable();
		// 	}
		// 	this.configForm.updateValueAndValidity();
		// });
	}

	ngOnInit() {
	}

	onFileChangeClick($event): void {
		this.configFromUploadFile = $event.target.files[0];
		this.isConfigUrlDisabled = this.configFromUploadFile != null;
	}

	onUndoFileClick(): void {
		this.configFromUploadFile = null;
		this.isConfigUrlDisabled = false;
	}

}
