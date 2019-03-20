import {SUBMIT_FORM_STARTED, SUBMIT_FORM_FAILURE, SUBMIT_FORM_SUCCESS} from "../actions/types";

const initialState = {};
export default (state = initialState, action) => {
    switch (action.type) {
        case SUBMIT_FORM_SUCCESS:
            return {
                ...state,
                fetching: false,
                result: action.payload
            };
        case SUBMIT_FORM_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.payload
            };
        case SUBMIT_FORM_STARTED:
            return {
                ...state,
                fetching: true
            };
        default:
            return state;
    }
}
