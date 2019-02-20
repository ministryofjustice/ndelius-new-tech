import { connect } from 'react-redux'
import frameNavigation from '../../shared/frameNavigation'

export default connect(state => ({navigate: state.navigate}), () => ({}))(frameNavigation)
