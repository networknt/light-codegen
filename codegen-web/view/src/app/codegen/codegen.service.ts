import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {AppState} from 'app/app.state';
import {CodegenStage} from './codegen.config';
import {CodegenActions} from 'app/codegen/codegen.state';
/**
 * Created by Nicholas Azar on 5/11/2017.
 */

@Injectable()
export class CodegenService {
	constructor(private store: Store<AppState>) {}

	setStage(stage: CodegenStage) {
		this.store.dispatch(CodegenActions.setCodegenStage(stage));
	}
}
