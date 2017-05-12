import {Component, OnInit} from '@angular/core';
import {CodegenService} from 'app/codegen/codegen.service';
import {Store} from '@ngrx/store';
import {getGeneratorTypes, getIsLoadingGeneratorTypes} from 'app/codegen/codegen.state';
import {AppState} from 'app/app.state';
import {Observable} from 'rxjs/Observable';
import {GENERATORS_DROPDOWN_CONFIG} from 'app/codegen/codegen.config';

@Component({
	selector: 'app-codegen',
	templateUrl: './codegen.component.html',
	styleUrls: ['./codegen.component.css']
})
export class CodegenComponent implements OnInit {

	generatorTypes: Observable<{[item: string]: string}>;
	isLoadingGeneratorTypes: Observable<boolean>;

	generatorTypesDropdownConfig = GENERATORS_DROPDOWN_CONFIG;

	constructor(private codegenService: CodegenService, private store: Store<AppState>) {
		this.generatorTypes = this.store.select(getGeneratorTypes);
		this.isLoadingGeneratorTypes = this.store.select(getIsLoadingGeneratorTypes);
	}

	ngOnInit() {
		this.codegenService.getGeneratorTypes();
	}

}
