import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {AppComponent} from './app.component';
import {HeaderModule} from 'app/header/header.module';
import {RouterModule} from '@angular/router';
import {appRoutes} from './app.routes';
import {CodegenModule} from './codegen/codegen.module';
import {environment} from '../environments/environment';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {combineReducers, StoreModule} from '@ngrx/store';
import {compose} from '@ngrx/core';
import {storeFreeze} from 'ngrx-store-freeze';

import * as fromCodegen from './codegen/codegen.state';
import {CodegenCommonModule} from 'app/common/codegen-common.module';

const reducers = {
	codegen: fromCodegen.codegen
};

export function reducer(state: any, action: any) {
	if (environment.production) {
		return combineReducers(reducers)(state, action);
	}
	return compose(storeFreeze, combineReducers)(reducers)(state, action);
}

export let MODULE_IMPORTS = [
	BrowserModule,
	FormsModule,
	HttpModule,
	BrowserAnimationsModule,
	HeaderModule,
	CodegenModule,
	CodegenCommonModule,
	StoreModule.provideStore(reducer),
	RouterModule.forRoot(appRoutes, {useHash: true})
];

if (!environment.production) {
	MODULE_IMPORTS.push(StoreDevtoolsModule.instrumentOnlyWithExtension());
}

@NgModule({
	declarations: [
		AppComponent
	],
	imports: [MODULE_IMPORTS],
	providers: [],
	bootstrap: [AppComponent]
})
export class AppModule {
}
