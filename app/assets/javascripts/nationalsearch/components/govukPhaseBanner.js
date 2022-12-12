import React from 'react'
import FeedbackLink from './feedbackLink'
import LegacySearchLink from '../containers/legacySearchLinkContainer'

const GovUkPhaseBanner = () => {
  return (
    <div className='govuk-phase-banner'>
      <p className='govuk-phase-banner__content'>
        <strong className='govuk-tag govuk-phase-banner__content__tag'>BETA</strong>
        <span className='govuk-phase-banner__text'>
          This is a new service – your <FeedbackLink tabIndex='1'>feedback</FeedbackLink> will help us to improve it.
          Access the <LegacySearchLink tabIndex='1'>previous search</LegacySearchLink> here.
        </span>
      </p>
        <p className='govuk-phase-banner__content'>
            <br/>
            We are planning to consolidate the two search screens which are currently available in Delius.  Please use the above feedback link to let us know of features you find most useful on both this and the old search screen.
      </p>
    </div>
  )
}

export default GovUkPhaseBanner
