import {initSummaryAnalytics} from './summary'
import {trackEvent} from '../../helpers/analyticsHelper'

jest.mock('../../helpers/analyticsHelper', () => ({
  trackEvent: jest.fn()
}))

describe('Summary component', () => {

  function addToMainTestDOM(partial) {
    return '<h1>Test summary component heading</h1>' +
      '<div>' +
      `${ partial }` +
      '  <details class="govuk-details">' +
      '    <summary class="govuk-details__summary">' +
      '      <span class="govuk-details__summary-text">' +
      '        Some details title' +
      '      </span>' +
      '    </summary>' +
      '    <div class="govuk-details__text">' +
      '      Some details text that is activated when clicking the summary link.' +
      '    </div>' +
      '  </details>' +
      '</div>'
  }

  describe('with the details component below caption text', () => {
    beforeEach(() => {
      document.body.innerHTML = addToMainTestDOM('<h2 class="govuk-caption-xl">Some caption text</h2>')
      initSummaryAnalytics()
    })

    afterEach(() => {
      trackEvent.mockClear()
    })

    test('should track the selected summary element click and pass the relevant data', () => {
      const summary = document.querySelector('summary')
      summary.click() // open
      expect(trackEvent).toHaveBeenCalledWith('open', 'PAROM1 - What to include', 'Test summary component heading > Some caption text')
    })

    test('should correctly track open/close state', () => {
      const summary = document.querySelector('summary')
      summary.click() // Open
      summary.click() // Close
      expect(trackEvent).toHaveBeenCalledWith('close', 'PAROM1 - What to include', 'Test summary component heading > Some caption text')
    })
  })

  describe('with the details component below an input field label', () => {
    beforeEach(() => {
      document.body.innerHTML = addToMainTestDOM('<label class="govuk-label"><span>Some text label</span></label>')
      initSummaryAnalytics()
    })

    afterEach(() => {
      trackEvent.mockClear()
    })

    test('should track the selected summary element click and pass the relevant data', () => {
      const summary = document.querySelector('summary')
      summary.click() // open
      expect(trackEvent).toHaveBeenCalledWith('open', 'PAROM1 - What to include', 'Test summary component heading > Some text label')
    })

    test('should correctly track open/close state', () => {
      const summary = document.querySelector('summary')
      summary.click() // Open
      summary.click() // Close
      expect(trackEvent).toHaveBeenCalledWith('close', 'PAROM1 - What to include', 'Test summary component heading > Some text label')
    })
  })
})
