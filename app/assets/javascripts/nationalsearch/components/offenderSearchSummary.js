import React from 'react'
import PropTypes from 'prop-types'
import OffenderSummaryTitle from '../containers/offenderSummaryTitleContainer'
import MT from '../containers/markableTextContainer'
import { matchesAnyHighlightedField, matchesHighlightedField } from './markableText'
import AddContactLink from '../containers/addContactLinkContainer'
import { officer, provider } from '../../helpers/offenderManagerHelper'

const OffenderSearchSummary = ({ offenderSummary, searchTerm }) => (
  <li id={`offenderSummary${offenderSummary.offenderId}`}>
    <div className='govuk-grid-row qa-offender-details-row'>
      <div className='govuk-grid-column-one-quarter govuk-!-margin-0 moj-!-text-align-center app-hide-tablet'>
        {offenderSummary.oneTimeNomisRef &&
        <img alt={`Image of ${offenderSummary.firstName} ${offenderSummary.surname}`} className='app-offender-image'
             src={`offender/oneTimeNomisRef/${encodeURIComponent(offenderSummary.oneTimeNomisRef)}/image`} width='120'
             height='150' />}
        {!offenderSummary.oneTimeNomisRef &&
        <img alt='No offender image available' className='app-offender-image' src='assets/images/NoPhoto@2x.png' width='120'
             height='150' />}
      </div>

      <div role='group' className='govuk-grid-column-three-quarters govuk-!-margin-0'>
        <div className='govuk-inset-text app-inset-text govuk-!-margin-top-0 govuk-!-margin-left-0 govuk-!-padding-top-0'>
          <p>
            <OffenderSummaryTitle {...offenderSummary} tabIndex='1' />
          </p>
          <p className='govuk-body'>
            <span id={`crn-label-${offenderSummary.offenderId}`} className='govuk-!-font-weight-bold'>CRN:&nbsp;</span>
            <span className='govuk-!-font-weight-bold govuk-!-margin-right-1' aria-labelledby={`crn-label-${offenderSummary.offenderId}`}>
              <MT text={offenderSummary.otherIds.crn} highlight={offenderSummary.highlight}
                  highlightFieldName='otherIds.crn' />
            </span>
            <Risk risk={offenderSummary.offenderProfile.riskColour} />
            <CurrentOffender current={offenderSummary.currentDisposal} />
            <span className='govuk-!-margin-left-1 govuk-!-margin-right-1'>
                <span aria-label='Gender'>
                  <MT text={offenderSummary.gender} highlight={offenderSummary.highlight}
                      highlightFieldName='gender' />,&nbsp;</span>
                <span aria-label='Age'>{offenderSummary.age}</span>
            </span>
            <br />
            <span id={`provider-${offenderSummary.offenderId}`}>
                <span id={`provider-label-${offenderSummary.offenderId}`}>Provider:&nbsp;</span>
                <span className='govuk-!-margin-right-1'
                      aria-labelledby={`provider-label-${offenderSummary.offenderId}`}>{provider(offenderSummary)}</span>
            </span>
            <br />
            <span id={`officer-${offenderSummary.offenderId}`}>
                <span id={`officer-label-${offenderSummary.offenderId}`}>Officer name:&nbsp;</span>
                <span className='govuk-!-margin-right-1'
                      aria-labelledby={`officer-label-${offenderSummary.offenderId}`}>{officer(offenderSummary)}</span>
            </span>
          </p>
          {matchesAnyHighlightedField(offenderSummary.highlight, ['otherIds.pncNumberLongYear', 'otherIds.pncNumberShortYear']) &&
          <p>
            <span id={`pncNumber-label-${offenderSummary.offenderId}`}>PNC:&nbsp;</span>
            <span id={`pncNumber-${offenderSummary.offenderId}`}
                  aria-labelledby={`pncNumber-label-${offenderSummary.offenderId}`}
                  className='govuk-!-margin-right-1 app-mark'>{offenderSummary.otherIds.pncNumber}</span>
          </p>
          }
          {matchesHighlightedField(offenderSummary.highlight, 'otherIds.nomsNumber') &&
          <p>
            <span id={`nomsNumber-label-${offenderSummary.offenderId}`}>NOMS:&nbsp;</span>
            <span id={`nomsNumber-${offenderSummary.offenderId}`}
                  aria-labelledby={`nomsNumber-label-${offenderSummary.offenderId}`} className='govuk-!-margin-right-1'>
              <MT text={offenderSummary.otherIds.nomsNumber} highlight={offenderSummary.highlight}
                  highlightFieldName='otherIds.nomsNumber' />
            </span>
          </p>
          }
          {matchesHighlightedField(offenderSummary.highlight, 'otherIds.niNumber') &&
          <p>
            <span id={`niNumber-label-${offenderSummary.offenderId}`}>National Insurance Number:&nbsp;</span>
            <span id={`niNumber-${offenderSummary.offenderId}`}
                  aria-labelledby={`niNumber-label-${offenderSummary.offenderId}`} className='govuk-!-margin-right-1'><MT
              text={offenderSummary.otherIds.niNumber} highlight={offenderSummary.highlight}
              highlightFieldName='otherIds.niNumber' /></span>
          </p>
          }
          {matchesHighlightedField(offenderSummary.highlight, 'otherIds.croNumberLowercase') &&
          <p>
            <span id={`croNumber-label-${offenderSummary.offenderId}`}>CRO:&nbsp;</span>
            <span id={`croNumber-${offenderSummary.offenderId}`}
                  aria-labelledby={`croNumber-label-${offenderSummary.offenderId}`} className='govuk-!-margin-right-1'><MT
              text={offenderSummary.otherIds.croNumber} highlight={offenderSummary.highlight}
              highlightFieldName='otherIds.croNumberLowercase' /></span>
          </p>
          }
          {matchesHighlightedField(offenderSummary.highlight, 'middleNames') &&
          <MiddleNames middleNames={offenderSummary.middleNames} highlight={offenderSummary.highlight}
                       highlightFieldName='middleNames' />
          }
          {matchesAnyHighlightedField(offenderSummary.highlight, ['offenderAliases.surname', 'offenderAliases.firstName']) &&
          offenderSummary.aliases.map((alias, index) => (
            <p key={index}>
              <span className='govuk-!-margin-right-1'>Alias:</span>
              <span><MT text={alias.surname} highlight={offenderSummary.highlight}
                        highlightFieldName='offenderAliases.surname' /></span>
              <span>,&nbsp;</span>
              <span><MT text={alias.firstName} highlight={offenderSummary.highlight}
                        highlightFieldName='offenderAliases.firstName' /></span>
            </p>
          ))
          }
          {matchesHighlightedField(offenderSummary.highlight, 'previousSurnames') &&
          <PreviousSurname name={offenderSummary.previousSurname} highlight={offenderSummary.highlight} />
          }
          {matchesAnyHighlightedField(offenderSummary.highlight,
            [
              'contactDetails.addresses.buildingName',
              'contactDetails.addresses.streetName',
              'contactDetails.addresses.town',
              'contactDetails.addresses.county',
              'contactDetails.addresses.postcode']) &&
          offenderSummary.addresses.map((address, index) => (
            <p key={index}>
              <span className='govuk-!-margin-right-1'>Address:</span>
              <Address address={address} highlight={offenderSummary.highlight} />
            </p>
          ))
          }
          <p><AddContactLink tabIndex='1' firstName={offenderSummary.firstName} surname={offenderSummary.surname}
                             offenderId={offenderSummary.offenderId} rankIndex={offenderSummary.rankIndex} /></p>
        </div>
      </div>
    </div>
  </li>
)

OffenderSearchSummary.propTypes = {
  offenderSummary: PropTypes.shape({
    firstName: PropTypes.string.isRequired,
    surname: PropTypes.string.isRequired,
    middleNames: PropTypes.arrayOf(
      PropTypes.string.isRequired
    ),
    dateOfBirth: PropTypes.string.isRequired,
    otherIds: PropTypes.shape({
      crn: PropTypes.string.isRequired,
      pncNumber: PropTypes.string,
      nomsNumber: PropTypes.string,
      croNumber: PropTypes.string
    }).isRequired,
    risk: PropTypes.string,
    currentDisposal: PropTypes.string.isRequired,
    gender: PropTypes.string.isRequired,
    age: PropTypes.number.isRequired,
    aliases: PropTypes.arrayOf(
      PropTypes.shape({
        surname: PropTypes.string.isRequired,
        firstName: PropTypes.string.isRequired
      })
    ).isRequired,
    addresses: PropTypes.arrayOf(
      PropTypes.shape({
        buildingName: PropTypes.string,
        addressNumber: PropTypes.string,
        streetName: PropTypes.string,
        town: PropTypes.string,
        county: PropTypes.string,
        postcode: PropTypes.string
      })
    ).isRequired,
    previousSurname: PropTypes.string,
    offenderManagers: PropTypes.arrayOf(
      PropTypes.shape({
        staff: PropTypes.shape({
          forenames: PropTypes.string,
          surname: PropTypes.string
        }),
        probationArea: PropTypes.shape({
          description: PropTypes.string
        })
      }))
  }).isRequired,
  searchTerm: PropTypes.string.isRequired
}

const Risk = ({ risk }) => {
  if (risk) {
    return (<span aria-label={`risk alert colour`} className='govuk-!-margin-right-1'>Risk <span
      className={`app-risk-icon ${mapRiskColor(risk)}`} /><span className='govuk-visually-hidden'>{risk}</span>&nbsp;|</span>)
  }
  return (<span />)
}

const CurrentOffender = ({ current }) => {
  if (current && current === '1') {
    return (<span className='govuk-!-margin-right-1'>Current&nbsp;|</span>)
  }
  return (<span />)
}

const MiddleNames = ({ middleNames, highlight, highlightFieldName }) => (
  <p>
    <span className='govuk-!-margin-right-1'>Middle Names:</span>
    {middleNames.map((middleName, index) => (
      <span key={index}>
          <span><MT text={middleName} highlight={highlight} highlightFieldName={highlightFieldName} /></span>
          <span>&nbsp;</span>
      </span>
    ))}
  </p>
)

const Address = ({ address, highlight }) => {
  const lines = [
    {
      highlightFieldName: 'contactDetails.addresses.buildingName',
      text: address.buildingName
    },
    {
      highlightFieldName: 'contactDetails.addresses.streetName',
      text: firstAddressLine(address.addressNumber, address.streetName)
    },
    {
      highlightFieldName: 'contactDetails.addresses.town',
      text: address.town
    },
    {
      highlightFieldName: 'contactDetails.addresses.county',
      text: address.county
    },
    {
      highlightFieldName: 'contactDetails.addresses.postcode',
      text: address.postcode
    }].filter(line => !!line.text)

  return (
    <span>
      {lines.map((line, index) => (
        <span className='govuk-!-margin-right-1' key={index}><MT text={line.text} highlight={highlight}
                                                       highlightFieldName={line.highlightFieldName} />{index + 1 < lines.length ? ',' : ''}</span>
      ))}
    </span>
  )
}

const firstAddressLine = (number = '', street = '') => `${number} ${street}`.trim()

const PreviousSurname = ({ name, highlight }) => {
  if (name) {
    return (<p>
      <span className='govuk-!-margin-right-1'>Previous surname:</span>
      <span><MT text={name} highlight={highlight} highlightFieldName='previousSurnames' /></span>
    </p>)
  }
  return (<span />)
}

const mapRiskColor = (risk = '') => {
  return `app-risk-icon--${ risk.toLowerCase() }`
}

export { OffenderSearchSummary as default, Address }
