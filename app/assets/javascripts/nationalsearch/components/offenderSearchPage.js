import React from 'react'
import { Link } from 'react-router-dom'

import OffenderSearchResults from '../containers/offenderSearchResultsContainer'
import OffenderSearch from '../containers/offenderSearchContainer'
import FrameNavigation from '../containers/frameNavigationContainer'
import AddNewOffenderLink from '../containers/addNewOffenderLinkContainer'
import Suggestions from '../containers/suggestionsContainer'
import GovUkPhaseBanner from './govukPhaseBanner'
import SearchFooter from './searchFooter'
import PropTypes from 'prop-types'

const OffenderSearchPage = ({ firstTimeIn, showWelcomeBanner, reloadRecentSearch }) => {
  if (firstTimeIn) {
    if (typeof gtag === 'function') {
      virtualPageLoad('')
    }
    reloadRecentSearch()
  }

  return (
    <div className='govuk-width-container govuk-!-padding-top-0'>
      <main className='govuk-main-wrapper govuk-!-padding-top-0' id='root'>

        <GovUkPhaseBanner />

        <div className='moj-interrupt govuk-!-padding-5 govuk-!-margin-bottom-0 app-position-relative' style={{'minHeight': '148px'}}>

          <h1 className='govuk-heading-l moj-!-color-white govuk-!-margin-bottom-2'>Search for people on probation</h1>

          <div className='app-national-search-add'>
            <AddNewOffenderLink tabIndex='3' />
          </div>

          <OffenderSearch />

          <div className='app-national-search-help'>
            <Link tabIndex='2' to='help' title='View tips for getting better results'
                  className='govuk-body govuk-link govuk-link--no-visited-state govuk-!-font-weight-bold moj-!-color-white'>Tips
              for getting better results</Link>
          </div>

          <div className='govuk-grid-row'>
            <div className='govuk-grid-column-two-thirds'>
              <Suggestions />
            </div>
          </div>

        </div>

        {showWelcomeBanner && <SearchFooter />}
        {!showWelcomeBanner && <OffenderSearchResults />}

        <FrameNavigation />

      </main>
    </div>
  )
}

OffenderSearchPage.propTypes = {
  firstTimeIn: PropTypes.bool.isRequired,
  showWelcomeBanner: PropTypes.bool.isRequired,
  reloadRecentSearch: PropTypes.func.isRequired
}

export default OffenderSearchPage
