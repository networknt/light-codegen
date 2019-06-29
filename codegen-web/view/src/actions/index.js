import {history} from '../App';
import { LOAD_MENU, SUBMIT_FORM_STARTED, SUBMIT_FORM_SUCCESS, SUBMIT_FORM_FAILURE } from './types';

export function loadMenu(host) {
    return {
        type: LOAD_MENU,
        payload: host
    }
}

export function submitForm(action) {
    return async (dispatch) => {
        dispatch({type: SUBMIT_FORM_STARTED});
        const request = {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'env_tag': action.data.release
            },
            body: JSON.stringify(action)
        };
        console.log(action.data.release);
        try {
            const response = await fetch('/codegen', request);
            const data = await response.blob();
            const filename = 'light-project.zip';
            if (typeof window.navigator.msSaveBlob !== 'undefined') {
                window.navigator.msSaveBlob(data, filename);
            } else {
                const url = window.URL.createObjectURL(data);
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', filename);
                document.body.appendChild(link);
                link.click();
            }
            //console.log("data", data);
            dispatch({ type: SUBMIT_FORM_SUCCESS, payload: data });
            history.push(action.success);
        } catch(e) {
            //console.log("error " + e.toString());
            dispatch({ type: SUBMIT_FORM_FAILURE, error: e.toString()})
        }
    }
}
