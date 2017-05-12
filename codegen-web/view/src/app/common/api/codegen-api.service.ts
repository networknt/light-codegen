/**
 * Created by Nicholas Azar on 5/11/2017.
 */


import {CodegenRepositoryService} from 'app/common/api/codegen-repository.service';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class CodegenApiService {

	constructor(private codegenRepository: CodegenRepositoryService) {}

	getGeneratorTypes(): Observable<{[item: string]: string}> {
		return this.codegenRepository.getGeneratorTypes();
	}

}