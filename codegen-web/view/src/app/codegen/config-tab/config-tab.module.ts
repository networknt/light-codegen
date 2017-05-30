import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import { ConfigTabComponent } from './config-tab.component';
import {AddedSchemasModule} from 'app/codegen/added-schemas/added-schemas.module';
import {ConfigService} from 'app/codegen/config-tab/config.service';
import { AddConfigFormComponent } from './add-config-form/add-config-form.component';
import {ReactiveFormsModule} from '@angular/forms';
import {CodegenCommonModule} from 'app/common/codegen-common.module';

@NgModule({
	imports: [CommonModule, AddedSchemasModule, ReactiveFormsModule, CodegenCommonModule],
	declarations: [ConfigTabComponent, AddConfigFormComponent],
	exports: [ConfigTabComponent],
	providers: [ConfigService]
})
export class ConfigTabModule {}
