import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodegenApiModule} from 'app/common/api/codegen-api.module';
import {DisableFormControlDirective} from 'app/common/app.directives';

@NgModule({
	imports: [CommonModule, CodegenApiModule],
	declarations: [DisableFormControlDirective],
	exports: [DisableFormControlDirective]
})
export class CodegenCommonModule {
}
