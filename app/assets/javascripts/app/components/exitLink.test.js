'use strict'

import {initExitLink} from './exitLink'

describe('Exit link component', () => {

  const mockFormSubmitMethod = window.HTMLFormElement.prototype.submit = jest.fn()

  beforeEach(() => {
    document.body.innerHTML = '<form id="ndForm"><input id="jumpNumber" /><button id="exitLink">Exit</button></form>'
    initExitLink()

    document.getElementById('exitLink').click()
  })

  afterEach(() => {
    mockFormSubmitMethod.mockClear()
  })

  test('should set jumpNumber to zero', () => {
    expect(document.getElementById('jumpNumber').value).toBe('0')
  })

  test('should submit the form', () => {
    expect(mockFormSubmitMethod).toHaveBeenCalled()
  })
})
