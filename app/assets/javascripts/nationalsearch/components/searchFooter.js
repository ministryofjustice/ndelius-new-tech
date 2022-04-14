import React, { Fragment } from 'react'

const SearchFooter = () => (
  <Fragment>

    <div className='govuk-warning-text moj-warning-text govuk-!-padding-2 govuk-!-padding-left-5'>
      <p className='govuk-body-l govuk-!-margin-0'>Expand <span className='govuk-!-font-weight-bold'>the results</span> by
        selecting <span className='govuk-!-font-weight-bold'>'No'</span> to <span className='govuk-!-font-weight-bold'>'Match all terms'</span>
      </p>
    </div>

    <h3 className='govuk-heading-m govuk-!-margin-top-8'>Find people by using any combination of:</h3>
    <ul className='govuk-list govuk-list--bullet'>
      <li>name and date of birth</li>
      <li>CRN, PNC and National Insurance numbers</li>
      <li>aliases and other recorded dates of birth</li>
      <li>towns and postcodes</li>
    </ul>

  </Fragment>
)

export default SearchFooter
