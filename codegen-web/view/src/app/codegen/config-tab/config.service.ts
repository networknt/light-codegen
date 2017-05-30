
import {Injectable} from '@angular/core';
import {CodegenApiService} from '../../common/api/codegen-api.service';
import {Store} from '@ngrx/store';
import {AppState} from '../../app.state';
import {ConfigActions} from 'app/codegen/config-tab/config.state';

@Injectable()
export class ConfigService {

	constructor(private store: Store<AppState>, private codegenApi: CodegenApiService) {}

	getConfigSchema(framework: string) {
		this.store.dispatch(ConfigActions.fetchSchema());
		this.codegenApi.getConfigSchema(framework).subscribe((configSchema: any) => {
			this.store.dispatch(ConfigActions.schemaFetched(configSchema));
		});
	}


}