import React, { Component, Fragment } from 'react'
import * as PropTypes from 'prop-types'
import GovUkPhaseBanner from './govukPhaseBanner'
import ErrorMessage from './errorMessage'
import OffenderIdentity from '../containers/offenderIdentityContainer'
import OffenderDetails from '../containers/offenderDetailsContainer'
import Registrations from '../containers/registrationsContainer'
import SeriousRegistrations from '../containers/seriousRegistrationsContainer'
import Convictions from '../containers/convictionsContainer'
import Notes from '../containers/notesContainer'
import OffenderManager from '../containers/offenderManagerContainer'

import { nodeListForEach } from 'govuk-frontend/common'
import Accordion from 'govuk-frontend/components/accordion/accordion'

class OffenderSummaryPage extends Component {
  constructor (props) {
    super(props)
  }

  componentWillMount () {
    const {getOffenderDetails} = this.props
    getOffenderDetails()
  }

  componentDidMount () {
    setTimeout(() => {
      const $accordions = document.querySelectorAll('[data-module="accordion"]')
      nodeListForEach($accordions, function ($accordion) {
        new Accordion($accordion).init()
      })
    }, 5000)
  }

  render () {
    const {fetching, error} = this.props
    return (
      <Fragment>
        <GovUkPhaseBanner/>
        { !fetching && !error &&
        <div className="qa-main-content">

          <OffenderIdentity/>
          <SeriousRegistrations/>
          <div className="govuk-accordion" data-module="accordion" id="accordion-offender-summary">
            <Registrations/>
            <Convictions/>
            <OffenderManager/>
            <OffenderDetails/>
          </div>
          <Notes/>

        </div>
        }
        { !fetching && error &&
        <ErrorMessage
          message="Unfortunately, we cannot display you the offender's information at the moment. Please try again later."/>
        }
      </Fragment>
    )
  }
}

OffenderSummaryPage.propTypes = {
  getOffenderDetails: PropTypes.func,
  fetching: PropTypes.bool,
  error: PropTypes.bool
}

export default OffenderSummaryPage