import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenSchemaTabComponent} from './schema-tab.component';
import {NgSelectizeModule} from 'ng-selectize';
import { AddSchemaFormComponent } from './add-schema-form/add-schema-form.component';
import {SchemaService} from 'app/codegen/schema-tab/schema.service';
import {ReactiveFormsModule} from '@angular/forms';
import {AddedSchemasModule} from 'app/codegen/added-schemas/added-schemas.module';

@NgModule({
	imports: [CommonModule, NgSelectizeModule, ReactiveFormsModule, AddedSchemasModule],
	declarations: [CodegenSchemaTabComponent, AddSchemaFormComponent],
	exports: [CodegenSchemaTabComponent],
	providers: [SchemaService]
})
export class CodegenSchemaTabModule {
}
