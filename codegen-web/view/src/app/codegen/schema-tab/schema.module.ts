import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenSchemaTabComponent} from './schema-tab.component';
import {NgSelectizeModule} from 'ng-selectize';
import { SchemasAddedViewComponent } from './schemas-added-view/schemas-added-view.component';
import { AddSchemaFormComponent } from './add-schema-form/add-schema-form.component';
import {SchemaService} from 'app/codegen/schema-tab/schema.service';
import {ReactiveFormsModule} from '@angular/forms';

@NgModule({
	imports: [CommonModule, NgSelectizeModule, ReactiveFormsModule],
	declarations: [CodegenSchemaTabComponent, SchemasAddedViewComponent, AddSchemaFormComponent],
	exports: [CodegenSchemaTabComponent],
	providers: [SchemaService]
})
export class CodegenSchemaModule {
}
