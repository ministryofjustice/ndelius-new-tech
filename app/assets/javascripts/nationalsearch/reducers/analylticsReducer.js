import {
    VISIT_COUNTS,
    FETCHING_VISIT_COUNTS,
    TIME_RANGE,
    THIS_WEEK
} from '../actions/analytics'

const analytics = (state = {uniqueUserVisits: 0, allVisits: 0, fetching: false, timeRange: THIS_WEEK}, action) => {
    switch (action.type) {
        case VISIT_COUNTS:
            return {
                ...state,
                uniqueUserVisits: action.uniqueUserVisits,
                allVisits: action.allVisits,
                fetching: false
            };
        case FETCHING_VISIT_COUNTS:
            return {
                ...state,
                fetching: true
            };
        case TIME_RANGE:
            return {
                ...state,
                timeRange: action.timeRange
            }
        default:
            return state
    }
};

export default analytics

