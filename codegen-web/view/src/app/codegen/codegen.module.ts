import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenComponent} from './codegen.component';
import {CodegenService} from './codegen.service';
import {CodegenSchemaModule} from 'app/codegen/schema-tab/schema.module';

@NgModule({
	imports: [CommonModule, CodegenSchemaModule],
	declarations: [CodegenComponent],
	exports: [CodegenComponent],
	providers: [CodegenService]
})
export class CodegenModule {
}
