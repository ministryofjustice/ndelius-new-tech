'use strict'

import {initReportNavigation} from './reportNavigation'

describe('Report navigation component', () => {

  const mockFormSubmitMethod = window.HTMLFormElement.prototype.submit = jest.fn()

  beforeEach(() => {
    document.body.innerHTML =
      '<nav class="moj-subnav">' +
      '  <ul class="moj-subnav__section">' +
      '    <li class="moj-subnav__section-item">' +
      '      <a href="#" class="js-nav-item moj-subnav__link govuk-link" data-target="1">Navigation item 1</a>' +
      '    </li>' +
      '    <li class="moj-subnav__section-item">' +
      '      <a href="#" class="js-nav-item moj-subnav__link govuk-link" data-target="2">Navigation item 2</a>' +
      '    </li>' +
      '  </ul>' +
      '</nav>' +
      '<form id="ndForm"><input id="jumpNumber" value="0" /></form>'

    initReportNavigation()
  })

  afterEach(() => {
    mockFormSubmitMethod.mockClear()
  })

  describe('when clicking a navigation item', () => {
    beforeEach(() => {
      document.querySelectorAll('.js-nav-item')[1].click()
    })

    test('should set jumpNumber to the selected report section', () => {
      expect(document.getElementById('jumpNumber').value).toBe('2')
    })

    test('should submit the form', () => {
      expect(mockFormSubmitMethod).toHaveBeenCalled()
    })
  })

  describe('when clicking the currently selected navigation item', () => {
    beforeEach(() => {
      document.querySelectorAll('.moj-subnav__section-item')[1].classList.add('moj-subnav__section-item--current')
      document.querySelectorAll('.js-nav-item')[1].click()
    })

    test('should NOT set jumpNumber to the selected report section', () => {
      expect(document.getElementById('jumpNumber').value).toBe('0')
    })

    test('should NOT submit the form', () => {
      expect(mockFormSubmitMethod).not.toHaveBeenCalled()
    })
  })
})
