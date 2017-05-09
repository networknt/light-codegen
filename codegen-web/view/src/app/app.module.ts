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


@NgModule({
	declarations: [
		AppComponent
	],
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		BrowserAnimationsModule,
		HeaderModule,
		CodegenModule,
		RouterModule.forRoot(appRoutes, {useHash: true})
	],
	providers: [],
	bootstrap: [AppComponent]
})
export class AppModule {
}
