import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenComponent} from './codegen.component';

@NgModule({
	imports: [
		CommonModule
	],
	declarations: [CodegenComponent],
	exports: [CodegenComponent]
})
export class CodegenModule {
}
