import React from 'react'
import { Link } from 'react-router-dom'
import GovUkPhaseBanner from './govukPhaseBanner'
import SearchHintBox from './searchHintBox'

const HelpPage = () => {
  return (<div>
    <div className='govuk-width-container govuk-!-padding-top-0'>
      <main className='govuk-main-wrapper govuk-!-padding-top-0' id='root'>

        <GovUkPhaseBanner basicVersion />
        <div className='key-content'>

          <p className='govuk-!-margin-top-4'>
            <Link to='nationalSearch' className='govuk-back-link'>Back to New Search</Link>
          </p>

          <h1 className='govuk-heading-l'>Search tips</h1>

          <ul className='govuk-list govuk-list--bullet'>
            <li>
              <h2 className='govuk-heading-m govuk-!-margin-0'>Refine the results by selecting ‘Yes’ to
                ‘Match all terms’</h2>
              <p className='govuk-!-margin-top-1'>This will search on all of the information you enter.</p>
            </li>
          </ul>

          <ul className='govuk-list govuk-list--bullet'>
            <li>
              <h2 className='govuk-heading-m govuk-!-margin-0'>Expand the results by selecting ‘No’ to
                ‘Match all terms’</h2>
              <p className='govuk-!-margin-top-1'>This will search on any of the information you enter.</p>
            </li>
          </ul>

          <ul className='govuk-list govuk-list--bullet'>
            <li>
              <h2 className='govuk-heading-m govuk-!-margin-0'>Search by name and date of birth at the same
                time</h2>
              <p className='govuk-!-margin-top-1 govuk-!-margin-bottom-0'>For example, <strong>'John Smith 23/06/1986'</strong> - the
                results will be based on all those search terms.</p>

              <div className='govuk-!-margin-2' />
              <SearchHintBox hint='John Smith 23/06/1986' />
            </li>
          </ul>

          <br />

          <ul className='govuk-list govuk-list--bullet'>
            <li>
              <h2 className='govuk-heading-m govuk-!-margin-0'>Search by town, postcode at the same time as
                name</h2>
              <p className='govuk-!-margin-top-1 govuk-!-margin-bottom-0'>For example, <strong>'John Smith S1
                1AB'</strong> or <strong>'John Smith Sheffield'</strong> and so on. The search box can
                handle multiple search terms simultaneously.</p>

              <div className='govuk-!-margin-2' />

              <span className='app-float-left app-float-left__not-narrow'><SearchHintBox hint='John Smith S1 1AB' /></span>

              <p className='app-search-hint-middle govuk-body-m govuk-!-font-weight-bold app-float-left app-float-left__not-narrow'>Or</p>

              <span className='app-float-left app-float-left__not-narrow'><SearchHintBox hint='John Smith Sheffield' /></span>

              <div className='app-clearfix' />
            </li>
          </ul>

          <ul className='govuk-list govuk-list--bullet govuk-!-margin-top-2'>
            <li>
              <h2 className='govuk-heading-m govuk-!-margin-0'>Include CRN, CRO, PNC,
                National Insurance and NOMS numbers in your search</h2>
              <p className='govuk-!-margin-top-1'>Using unique numbers will achieve more accurate results.</p>

              <div className='govuk-!-margin-2' />
              <SearchHintBox hint='X087946' />
            </li>
          </ul>

          <ul className='govuk-list govuk-list--bullet govuk-!-margin-top-2'>
            <li>
              <h2 className='govuk-heading-m govuk-!-margin-0'>Include telephone numbers in your search</h2>
              <p className='govuk-!-margin-top-1'>Telephone number searching can be included on thier own or alongside other search terms.
                Telephone number terms need to contain more than 5 digits.
                Avoid including spaces in telephone numbers these will not match when <strong>'Match all Terms'</strong> is selected.
                Partial telephone numbers will also match as long as the number of digits in the search term is greater than 5.</p>

              <div className='govuk-!-margin-2' />
              <SearchHintBox hint='0779876767' />
            </li>
          </ul>

          <p>&nbsp;</p>

          <Link to='nationalSearch'><span className='app-icon app-icon__prev-arrow govuk-!-margin-right-1' />
            <span className='govuk-body-m govuk-link govuk-link--no-visited-state moj-link--no-underline'>Previous</span><br />
            <span className='govuk-body-xs govuk-link govuk-link--no-visited-state moj-link--no-underline'>New Search</span>
          </Link>

        </div>
      </main>
    </div>
  </div>)
}

export default HelpPage
