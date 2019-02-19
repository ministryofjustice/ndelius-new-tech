import React from 'react';
import PropTypes from 'prop-types';

const OffenderSearch = ({ searchTerm, probationAreasFilter, search, searchType }) => {

    const onSubmit = (event) => {
        event.preventDefault();
        let inputValue = document && document.getElementById ? document.getElementById('searchTerms').value : void 0;
        search(searchTerm || inputValue, searchType, probationAreasFilter);
    };

    return (
        <form role="search" className="padding-left-right" onSubmit={ (event) => onSubmit(event) }>
            <label htmlFor="searchTerms" className='visually-hidden'>Results will be updated as you type</label>
            <input id="searchTerms" name="searchTerms" type="search" tabIndex="1"
                   autoFocus={ true } className="form-control national-search"
                   placeholder="Any combination of names, dates of birth, ID numbers, towns and postcodes"
                   value={ searchTerm }
                   onChange={ event => search(event.target.value, searchType, probationAreasFilter) }/>
        </form>
    );
};

OffenderSearch.propTypes = {
    searchTerm: PropTypes.string.isRequired,
    search: PropTypes.func.isRequired,
    probationAreasFilter: PropTypes.arrayOf(PropTypes.string).isRequired,
    searchType: PropTypes.string.isRequired
};

export default OffenderSearch;
