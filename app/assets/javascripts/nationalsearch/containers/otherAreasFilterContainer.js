import { connect } from 'react-redux'
import areaFilter from '../components/filter'
import {addAreaFilter, removeAreaFilter, search} from '../actions/search'

export default connect(
    state => ({
        searchTerm: state.search.searchTerm,
        filterValues: removeMyProbationAreas(state.search.byProbationArea, state.search.myProbationAreas, state.search.probationAreasFilter),
        currentFilter: Object.getOwnPropertyNames(state.search.probationAreasFilter),
        name: 'all-providers',
        title: 'Other providers'
    }),
    dispatch => ({
        addToFilter: (probationAreaCode, probationAreaDescription)  => dispatch(addAreaFilter(probationAreaCode, probationAreaDescription)),
        removeFromFilter: probationAreaCode => dispatch(removeAreaFilter(probationAreaCode)),
        search: (searchTerm, probationAreasFilter) => dispatch(search(searchTerm, probationAreasFilter))
    })
)(areaFilter)

const addZeroResultsSelectedAreas = (byProbationArea, probationAreasFilter) => {
    return  Object.getOwnPropertyNames(probationAreasFilter)
        .filter(code => isNotInAggregation(code, byProbationArea))
        .map(code => ({
            code,
            description: probationAreasFilter[code],
            count: 0
        })).concat(byProbationArea)

}

const isNotInAggregation = (code, byProbationArea) => byProbationArea.filter(area => area.code === code).length === 0
export const removeMyProbationAreas = (byProbationArea, myProbationAreas, probationAreasFilter) => {

    return addZeroResultsSelectedAreas(byProbationArea, probationAreasFilter).reduce((updatedByProbationArea, area) => {
        if (!myProbationAreas[area.code]) {
            updatedByProbationArea.push(area)
        }
        return updatedByProbationArea
    }, [])
}

