'use strict'

import {formWithZeroJumpNumber} from '../utilities/formWithZeroJumpNumber'
import {promisifyXMLHttpRequest} from '../utilities/xhrPromisify'
import {initViewDraftLinks} from './viewDraftReport'
import {trackEvent} from '../../helpers/analyticsHelper'

jest.mock('../utilities/xhrPromisify', () => ({
  promisifyXMLHttpRequest: jest.fn().mockImplementation(() => {
    return new Promise((resolve) => {
      process.nextTick(() => resolve())
    })
  })
}))

jest.mock('../../helpers/analyticsHelper', () => ({
  trackEvent: jest.fn()
}))

describe('View draft component', () => {

  beforeEach(() => {
    document.body.innerHTML =
      '<h1>Test View Draft</h1>' +
      '<form id="ndForm" action="/some/form/url"><input id="jumpNumber" value="2" type="hidden" /></form>' +
      '<button id="draftReport" class="govuk-button" type="button" data-target="/some/url">View draft report</button>' +
      '<a href="#" class="js-view-draft">View draft link</a>'
  })

  describe('view draft button', () => {

    beforeEach(() => {
      initViewDraftLinks()
      document.getElementById('draftReport').click()
    })

    afterEach(() => {
      promisifyXMLHttpRequest.mockClear()
    })

    test('should POST form data with jumpNumber changed to zero to the specified endpoint', () => {
      const formData = formWithZeroJumpNumber(document.getElementById('ndForm'))
      expect(promisifyXMLHttpRequest).toHaveBeenCalledWith({ 'body': formData, 'method': 'POST', 'url': '/some/form/url/save' })
    })

    test('should have tracked the click event', () => {
      expect(trackEvent).toHaveBeenCalledWith('click', 'SFR - View draft', 'Test View Draft')
    })
  })

  describe('view draft link', () => {

    beforeEach(() => {
      initViewDraftLinks()
      document.querySelector('.js-view-draft').click()
    })

    test('should have tracked the click event', () => {
      expect(trackEvent).toHaveBeenCalledWith('click', 'SFR - View draft', 'Test View Draft')
    })
  })
})
