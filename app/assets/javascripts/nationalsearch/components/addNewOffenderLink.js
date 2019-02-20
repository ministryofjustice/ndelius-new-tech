import React from 'react'
import PropTypes from 'prop-types'

const AddNewOffenderLink = ({addNewOffender, tabIndex}) => (
  <a tabIndex={ tabIndex } href='javascript:' title="Add a new offender" className="clickable white"
     onClick={ addNewOffender }>Add a new offender</a>
)

AddNewOffenderLink.propTypes = {
  addNewOffender: PropTypes.func.isRequired,
  tabIndex: PropTypes.string
}

export default AddNewOffenderLink
