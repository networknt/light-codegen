import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenComponent} from './codegen.component';
import {CodegenService} from './codegen.service';
import {CodegenSchemaTabModule} from 'app/codegen/schema-tab/schema-tab.module';
import {ConfigTabModule} from 'app/codegen/config-tab/config-tab.module';

@NgModule({
	imports: [CommonModule, CodegenSchemaTabModule, ConfigTabModule],
	declarations: [CodegenComponent],
	exports: [CodegenComponent],
	providers: [CodegenService]
})
export class CodegenModule {
}
