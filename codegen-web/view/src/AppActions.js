/**
 * Created by Nicholas Azar on 2017-07-10.
 */


class AppActions {

    static API_HOST = 'lightapi.net';
    static API_SERVICE = 'codegen';
    static API_VERSION = '0.0.1';
    static TEST_AUTH_KEY = 'Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTgwOTAxMTkyMCwianRpIjoiR2NpMGJQZXoxb0hxT1VzYUZ6WmRkdyIsImlhdCI6MTQ5MzY1MTkyMCwibmJmIjoxNDkzNjUxODAwLCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJjb2RlZ2VuLnIiLCJjb2RlZ2VuLnciLCJzZXJ2ZXIuaW5mby5yIl19.MMIjxGlQknwtlizh80wX1oB75N8wfhqMttP7i3mpKwBa-zUKZcjgtE4rmc39qYXPti9ge3uGHWCQdMOlimf4Psoah-qtsQmZhuCOwejN_OhwvlIbLxCYYP9WaQM_zyuDv6luFO5ETsZQ0-QkxQHkBq1Y3D0VpHNuNlN3ulK5sK678XKw_2VNP6JM85hh1QW8pWYappkAledvyfJC3w5sUWdi3kP_rTGiumXYA0ZoY-hdp9erlin0ClEZ7qPmHSHW_TFjuRr_6rDwE1Xg-U6wNk1hMMFF4161nfdPIhDhCsjG3J4hM0y0ZluI3BnQaGKwtqyDNIJGiiTkK1ckX1qaIw'

    static VALIDATE_JSON_UPLOAD_REQUEST = {
        action: "/api/multipart",
        multiple: false,
        showUploadList: true,
        accept: '.json',
        headers: {
            Authorization: AppActions.TEST_AUTH_KEY
        },
        data: {
            host: AppActions.API_HOST,
            service: AppActions.API_SERVICE,
            action: 'validateUploadFile',
            version: AppActions.API_VERSION
        }
    };

    static FRAMEWORK_REQUEST = {
        host: AppActions.API_HOST,
        service: AppActions.API_SERVICE,
        action: "listFramework",
        version: AppActions.API_VERSION
    };

    static GENERATE_REQUEST = (framework, model, config) => {return {
        host: AppActions.API_HOST,
        service: AppActions.API_SERVICE,
        action: "generate",
        version: AppActions.API_VERSION,
        generators: [
            {
                framework: framework,
                model: model,
                config: config
            }
        ]
    }}
}

export {AppActions}