import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenComponent} from './codegen.component';
import {CodegenService} from './codegen.service';

@NgModule({
	imports: [
		CommonModule
	],
	declarations: [CodegenComponent],
	exports: [CodegenComponent],
	providers: [CodegenService]
})
export class CodegenModule {
}
