/**
 * Created by Nicholas Azar on 5/11/2017.
 */


import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {RequestOptions, Headers, Http} from '@angular/http';
import {ActionRequest, APP_HOST, APP_SERVICE, APP_VERSION} from 'app/app.config';
import {API_URL} from 'app/common/api/codegen-repository.config';
import {Injectable} from '@angular/core';
import {ErrorObservable} from 'rxjs/observable/ErrorObservable';


@Injectable()
export class CodegenRepositoryService {

	constructor(private http: Http) {}

	getGeneratorTypes(): Observable<{[item: string]: string}> {
		const requestBody: ActionRequest = buildActionRequest('listFramework');

		return this.http.post(API_URL, requestBody, POST_REQUEST)
			.map(handleRequestResponse)
			.map((arrayOfGenerators: string[]) => {
				return arrayOfGenerators.map((generator: string) => {
					return {item: generator};
				});
			})
			.catch(CodegenRepositoryService.handleRequestError);
	}


	static handleRequestError(errorResponse: Response | any): ErrorObservable {
		let errorMessage: string;
		if (errorResponse instanceof Response && errorResponse.status !== 404) {
			const body = errorResponse.json() || '';
			const error = body['error'] || JSON.stringify(body);
			errorMessage = `${errorResponse.status} - ${errorResponse.statusText || ''} ${error}`;
		} else if (errorResponse instanceof ErrorObservable) {
			return Observable.throw(errorResponse.error);
		} else {
			errorMessage = errorResponse.message ? errorResponse.message : errorResponse.toString();
		}
		console.error(errorMessage);
		return Observable.throw(errorResponse);
	}
}

export function buildActionRequest(action: string): ActionRequest {
	return {
		host: APP_HOST,
		service: APP_SERVICE,
		version: APP_VERSION,
		action: action
	};
}

export function handleRequestResponse(res: Response | any) {
	const rawResponseBody = res.json();
	if (rawResponseBody && rawResponseBody.errors && rawResponseBody.errors.length > 0) {
		throw Observable.throw(rawResponseBody);
	}
	return rawResponseBody;
}

export const GET_HEADERS = {'Accept': 'application/json'};
export const POST_HEADERS = Object.assign({}, GET_HEADERS, {'Content-Type': 'application/json'});
export const GET_REQUEST: RequestOptions = new RequestOptions({headers: new Headers(GET_HEADERS)});
export const POST_REQUEST: RequestOptions = new RequestOptions({headers: new Headers(POST_HEADERS)});
