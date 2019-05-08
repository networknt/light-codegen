import { combineReducers } from 'redux';
import menuReducer from './menu';
import formReducer from './FormReducer';

export default combineReducers({
    menu: menuReducer,
    form: formReducer
});
