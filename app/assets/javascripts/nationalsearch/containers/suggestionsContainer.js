import {connect} from 'react-redux'
import suggestions from '../components/suggestions'
import {search} from '../actions/search'

export default connect(
    state => ({
        suggestions: state.search.suggestions,
        searchTerm: state.search.searchTerm
    }),
    dispatch => ({
        search: (searchTerm) => search(dispatch, searchTerm)
    })
)(suggestions)