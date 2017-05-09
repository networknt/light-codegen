import {Routes} from '@angular/router';
import {CodegenComponent} from './codegen/codegen.component';
/**
 * Created by Nicholas Azar on 5/8/2017.
 */

export const appRoutes: Routes = [
	{
		path: '',
		component: CodegenComponent
	}, {
		path: '**', redirectTo: ''
	}
];
