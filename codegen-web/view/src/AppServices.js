/**
 * Created by Nicholas Azar on 2017-07-15.
 */
import axios from 'axios';
import {AppActions} from './AppActions';

class AppServices {

    static JSON_API = '/api/json';

    static get_frameworks() {
        return axios.post(AppServices.JSON_API, AppActions.FRAMEWORK_REQUEST, {
            headers: {'Authorization': AppActions.TEST_AUTH_KEY}
        });
    }
}

export {AppServices}