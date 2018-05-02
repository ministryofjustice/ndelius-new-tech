import PropTypes from "prop-types";

const OffenderSearch = ({searchTerm, probationAreasFilter, search}) => {
    const onSubmit = (event) => {
        search(searchTerm, probationAreasFilter)
        event.preventDefault()
    }

    return (
        <form className="padding-left-right" onSubmit={(event) => onSubmit(event, searchTerm, search)}>
            <p className='visually-hidden' id='search-description'>Results will be updated as you type</p>
            <input tabIndex="1" role='searchbox' aria-label='search' aria-describedby="search-description" autoFocus={true} name='searchTerms' className="form-control national-search" placeholder="Any combination of names + dates of birth + ID numbers + towns + postcodes" value={searchTerm} onChange={event => search(event.target.value, probationAreasFilter)} />
        </form>
    )
};

OffenderSearch.propTypes = {
    searchTerm: PropTypes.string.isRequired,
    search: PropTypes.func.isRequired,
    probationAreasFilter: PropTypes.arrayOf(PropTypes.string).isRequired
};

export default OffenderSearch;