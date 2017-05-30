import {Component, OnInit, Output, EventEmitter, OnDestroy} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Schema} from 'app/codegen/schema-tab/schema.config';
import {Store} from '@ngrx/store';
import {AppState} from 'app/app.state';
import {getAddedSchemas, getSelectedSchema} from 'app/codegen/schema-tab/schema.state';
import {ConfigService} from 'app/codegen/config-tab/config.service';
import {Subscription} from 'rxjs/Subscription';
import {getLoadedSchema} from 'app/codegen/config-tab/config.state';

@Component({
	selector: 'codegen-config-tab',
	templateUrl: './config-tab.component.html',
	styleUrls: ['./config-tab.component.css']
})
export class ConfigTabComponent implements OnInit, OnDestroy {

	@Output() onBack: EventEmitter<void> = new EventEmitter<void>(false);

	addedSchemas: Observable<Schema[]>;
	selectedSchema: Observable<Schema>;
	loadedSchema: Observable<any>;
	selectedSchemaSubscription: Subscription;

	constructor(private store: Store<AppState>, private configService: ConfigService) {
		this.addedSchemas = this.store.select(getAddedSchemas);
		this.selectedSchema = this.store.select(getSelectedSchema);
		this.loadedSchema = this.store.select(getLoadedSchema);
	}

	ngOnInit() {
		this.selectedSchemaSubscription = this.selectedSchema.subscribe((selectedSchema: Schema) => {
			if (selectedSchema) {
				this.configService.getConfigSchema(selectedSchema.generatorType);
			}
		});

	}

	onBackClick() {
		this.onBack.emit();
	}

	ngOnDestroy() {
		if (this.selectedSchemaSubscription && !this.selectedSchemaSubscription.closed) {
			this.selectedSchemaSubscription.unsubscribe();
		}
	}
}
