import React from 'react'
import ReactDOM from 'react-dom'
import {Provider} from 'react-redux'
import {applyMiddleware, createStore} from 'redux'
import thunkMiddleware from 'redux-thunk'
import {BrowserRouter as Router, Route, Switch} from 'react-router-dom'

import FeatureSwitchPage from './feature/featureSwitchPage'

import reducer from './reducers'

const store = createStore(reducer, applyMiddleware(thunkMiddleware))

ReactDOM.render(
  <Provider store={store}>
    <Router>
      <Switch>
        <Route path='(.*)/features' component={() => <FeatureSwitchPage />} />
      </Switch>
    </Router>
  </Provider>,
  document.getElementById('content')
)
