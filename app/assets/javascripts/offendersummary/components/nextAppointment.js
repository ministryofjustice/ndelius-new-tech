import React, {Component, Fragment} from 'react';
import * as PropTypes from 'prop-types';
import ErrorMessage from './errorMessage'
import moment from 'moment'
import {staff} from '../../helpers/offenderManagerHelper'
import {dateFromISO} from '../../helpers/formatters'

class NextAppointment extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        const { getNextAppointment } = this.props;
        getNextAppointment();
    }

    render() {
        const {fetching, error, appointment, noNextAppointment} = this.props;

        return (
            <Fragment>
                {!fetching && !error &&
                    <AppointmentDetail appointment={appointment} noNextAppointment={noNextAppointment}/>
                }
                {!fetching && error &&
                <ErrorMessage
                    message="Unfortunately, we cannot display you the offender's next appointment at the moment. Please try again later."/>
                }
            </Fragment>)
    }
}


export class AppointmentDetail extends Component {
    constructor(props) {
        super(props);

        this.details = null;

        this.setDetailsRef = element => this.details = element;
    }

    componentDidMount() {
        new window.GOVUKFrontend.Details(this.details).init();
    }

    render() {
        const {appointment, noNextAppointment} = this.props;

        return (
            <details className="govuk-details govuk-!-margin-top-0 qa-next-appointment" role="group" ref={this.setDetailsRef}>
                <summary className="govuk-details__summary" role="button"
                         aria-controls="details-content-appointment" aria-expanded="false">
                    <span className="govuk-details__summary-text"> Next appointment details </span>
                </summary>
                <div className="govuk-details__text moj-details__text--no-border" id="details-content-appointment"
                     aria-hidden="true">
                    <table className="govuk-table moj-table moj-table--split-rows" role="presentation">
                        <tbody>
                        <tr>
                            <th>Contact type</th>
                            <td>{!noNextAppointment && appointment.appointmentType.description || 'Unknown'}</td>
                        </tr>
                        <tr>
                            <th>Date</th>
                            <td>{!noNextAppointment && dateFromISO(appointment.appointmentDate) || 'Unknown'}</td>
                        </tr>
                        <tr>
                            <th>Start time</th>
                            <td>{!noNextAppointment && appointment.appointmentStartTime && moment(appointment.appointmentStartTime, 'H:mm:ss').format('H:mm') || 'Unknown'}</td>
                        </tr>
                        <tr>
                            <th>Location</th>
                            <td>{!noNextAppointment && appointment.officeLocation && appointment.officeLocation.description || 'Unknown'}</td>
                        </tr>
                        <tr>
                            <th>Provider</th>
                            <td>{!noNextAppointment && appointment.probationArea && appointment.probationArea.description || 'Unknown'}</td>
                        </tr>
                        <tr>
                            <th>Team</th>
                            <td>{!noNextAppointment && appointment.team && appointment.team.description || 'Unknown'}</td>
                        </tr>
                        <tr>
                            <th>Officer</th>
                            <td>{!noNextAppointment && appointment.staff && staff(appointment.staff) || 'Unknown'}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </details>
        )
    }

}

NextAppointment.propTypes = {
    getNextAppointment: PropTypes.func,
    fetching: PropTypes.bool,
    error: PropTypes.bool,
    noNextAppointment: PropTypes.bool,
    appointment: PropTypes.any
}
AppointmentDetail.propTypes = {
    noNextAppointment: PropTypes.bool,
    appointment: PropTypes.shape({
        appointmentType: PropTypes.shape({
            description: PropTypes.string.isRequired
        }).isRequired,
        appointmentDate: PropTypes.string.isRequired,
        appointmentStartTime: PropTypes.string,
        officeLocation: PropTypes.shape({
            description: PropTypes.string
        }),
        probationArea: PropTypes.shape({
            description: PropTypes.string
        }).isRequired,
        team: PropTypes.shape({
            description: PropTypes.string
        }),
        staff: PropTypes.shape({
            forenames: PropTypes.string,
            surname: PropTypes.string
        })
    })
}

export default NextAppointment