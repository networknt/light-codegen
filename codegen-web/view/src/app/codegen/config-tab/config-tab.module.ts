import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import { ConfigTabComponent } from './config-tab.component';
import {AddedSchemasModule} from 'app/codegen/added-schemas/added-schemas.module';
import {ConfigService} from 'app/codegen/config-tab/config.service';

@NgModule({
	imports: [CommonModule, AddedSchemasModule],
	declarations: [ConfigTabComponent],
	exports: [ConfigTabComponent],
	providers: [ConfigService]
})
export class ConfigTabModule {}
