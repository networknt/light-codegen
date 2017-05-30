import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenApiService} from 'app/common/api/codegen-api.service';
import {CodegenRepositoryService} from 'app/common/api/codegen-repository.service';

@NgModule({
	imports: [
		CommonModule
	],
	declarations: [],
	providers: [CodegenApiService, CodegenRepositoryService]
})
export class CodegenApiModule {
}
