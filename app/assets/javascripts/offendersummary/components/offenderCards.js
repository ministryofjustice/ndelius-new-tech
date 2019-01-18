import React, { Fragment } from 'react'
import * as PropTypes from 'prop-types'
import { convictionDescription, convictionSorter } from '../../helpers/convictionsHelper'
import { staff } from '../../helpers/offenderManagerHelper'

const OffenderCards = ({offenderConvictions, offenderManager}) => {

  let activeEvents
  let activeCount
  let totalCount

  if (offenderConvictions && offenderConvictions.convictions) {
    activeEvents = offenderConvictions.convictions.filter(conviction => conviction.active).sort(convictionSorter)
    activeCount = activeEvents.length
    totalCount = offenderConvictions.convictions.length
  }

  return (
    <div className="govuk-grid-row moj-flex">
      <div className="govuk-grid-column-one-half moj-flex">
        <div className="moj-interrupt moj-card moj-flex-child--stretch govuk-!-margin-top-1">
          <p className="qa-current-status govuk-heading-m govuk-!-margin-0 moj-!-color-white">
            { activeCount ? 'Current offender' : 'Not current' }
          </p>
          { !!activeCount && offenderManager && (
            <Fragment>
              { offenderManager.probationArea && (
                <p className="qa-provider govuk-body govuk-!-margin-0 moj-!-color-white">
                <span className="govuk-!-font-weight-bold">
                  Provider:
                </span> { offenderManager.probationArea.description }
                </p>
              ) }
              { offenderManager.staff && (
                <p className="qa-offender-manager govuk-body govuk-!-margin-0 moj-!-color-white">
                <span className="govuk-!-font-weight-bold">
                  Offender manager:
                </span> { staff(offenderManager.staff) }
                </p>
              ) }
            </Fragment>
          ) }
        </div>
      </div>
      <div className="govuk-grid-column-one-half moj-flex">
        <div className="moj-interrupt moj-card moj-flex-child--stretch govuk-!-margin-top-1">
          <p className="qa-events govuk-heading-m govuk-!-margin-0 moj-!-color-white">
            { (totalCount || 0) + (totalCount !== 1 ? ' events' : ' event') }
            { !!totalCount && (
              <span className="govuk-body govuk-!-margin-0 moj-!-color-white">&nbsp;({ activeCount || 0 } active)</span>
            ) }
          </p>
          { activeEvents && activeEvents.length && activeEvents[0].sentence && (
            <p className="qa-active-event govuk-body govuk-!-margin-0 moj-!-color-white">
              Last active event: { convictionDescription(activeEvents[0]) }
            </p>
          ) }
        </div>
      </div>
    </div>
  )
}

OffenderCards.propTypes = {
  offenderConvictions: PropTypes.shape({
    convictions: PropTypes.arrayOf(PropTypes.shape({
      active: PropTypes.bool,
      sentence: PropTypes.shape({
        description: PropTypes.string
      })
    }))
  }),
  offenderManager: PropTypes.shape({
    probationArea: PropTypes.shape({
      description: PropTypes.string
    }),
    staff: PropTypes.shape({
      forenames: PropTypes.string,
      surname: PropTypes.string
    })
  })
}

export default OffenderCards
