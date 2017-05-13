/**
 * Created by Nicholas Azar on 5/11/2017.
 */

export const APP_HOST = 'lightapi.net';
export const APP_SERVICE = 'codegen';
export const APP_VERSION = '0.0.1';

export interface ActionRequest {
	host: string;
	service: string;
	version: string;
	action: string;
}

export const DEFAULT_DROPDOWN_CONFIG: any = {
	highlight: false,
	create: false,
	persist: false,
	plugins: ['dropdown_direction', 'remove_button'],
	dropdownDirection: 'down'
};