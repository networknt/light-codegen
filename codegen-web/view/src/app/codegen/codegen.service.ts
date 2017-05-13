import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {AppState} from 'app/app.state';
import {CodegenStage} from './codegen.config';
import {CodegenActions} from 'app/codegen/codegen.state';
import {CodegenApiService} from 'app/common/api/codegen-api.service';
/**
 * Created by Nicholas Azar on 5/11/2017.
 */

@Injectable()
export class CodegenService {
	constructor(private store: Store<AppState>, private codegenApi: CodegenApiService) {}

	setStage(stage: CodegenStage) {
		this.store.dispatch(CodegenActions.setCodegenStage(stage));
	}

	getGeneratorTypes() {
		this.store.dispatch(CodegenActions.fetchGeneratorTypes());
		this.codegenApi.getGeneratorTypes().subscribe((generatorTypes: {[item: string]: string}) => {
			this.store.dispatch(CodegenActions.fetchedGeneratorTypes(generatorTypes));
		}, (error) => {
			this.store.dispatch(CodegenActions.fetchGeneratorTypesFailed());
		});
	}
}
