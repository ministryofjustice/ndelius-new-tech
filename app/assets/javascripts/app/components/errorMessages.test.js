'use strict'

import { reorderErrorMessages } from './errorMessages'

describe('Error messages component', () => {

  let $updatedErrorMessages

  beforeEach(() => {
    document.body.innerHTML =
      '<div aria-labelledby="error-summary-title" class="govuk-error-summary" role="alert" tabindex="-1">' +
      '  <h2 class="govuk-error-summary__title" id="error-summary-title">There is a problem</h2>' +
      '  <div class="govuk-error-summary__body">' +
      '    <ul class="govuk-list govuk-error-summary__list">' +
      '      <li><a href="#first-error" class="error-message">This first message will be replaced</a></li>' +
      '      <li><a href="#second-error" class="error-message">This second message will be replaced</a></li>' +
      '      <li><a href="#third-error" class="error-message">This third message will be replaced</a></li>' +
      '      <li><a href="#fourth-error" class="error-message">This fourth message will be replaced</a></li>' +
      '    </ul>' +
      '  </div>' +
      '</div>' +

      '<label class="govuk-label" for="firstInput">' +
      '  <div class="govuk-error-message" aria-hidden="true">This is the first new error message</div>' +
      '  <span id="firstInput-error" role="alert" class="govuk-visually-hidden">This is the first new error message</span>' +
      '</label>' +
      '<input type="hidden" name="firstInput" value="">' +

      '<label class="govuk-label" for="secondInput">' +
      '  <div class="govuk-error-message" aria-hidden="true">This is the second new error message</div>' +
      '  <span id="secondInput-error" role="alert" class="govuk-visually-hidden">This is the second new error message</span>' +
      '</label>' +
      '<input type="hidden" name="secondInput" value="">' +

      '<label class="govuk-label" for="thirdInput">' +
      '  <div class="govuk-error-message" aria-hidden="true">This is the third new error message</div>' +
      '  <span id="thirdInput-error" role="alert" class="govuk-visually-hidden">This is the third new error message</span>' +
      '</label>' +
      '<input type="hidden" name="thirdInput" value="">'
  })

  describe('Non-fixed order error messages', () => {

    beforeEach(() => {
      reorderErrorMessages()
      $updatedErrorMessages = document.querySelectorAll('.error-message')
    })

    test('should only display error messages based on the form errors', () => {
      expect($updatedErrorMessages.length).toEqual(3)
    })

    test('should replace error messages with form errors based on position within form', () => {
      expect($updatedErrorMessages[0].textContent).toBe('This is the first new error message')
      expect($updatedErrorMessages[1].textContent).toBe('This is the second new error message')
      expect($updatedErrorMessages[2].textContent).toBe('This is the third new error message')
    })

    test('should create target links to the relevant form error message', () => {
      expect($updatedErrorMessages[0].getAttribute('href')).toBe('#firstInput-error')
      expect($updatedErrorMessages[1].getAttribute('href')).toBe('#secondInput-error')
      expect($updatedErrorMessages[2].getAttribute('href')).toBe('#thirdInput-error')
    })
  })

  describe('Fixed order error messages', () => {

    beforeEach(() => {
      document.querySelector('.govuk-error-summary__list').classList.add('app-fixed-order')
      reorderErrorMessages()
      $updatedErrorMessages = document.querySelectorAll('.error-message')
    })

    test('should display existing error messages', () => {
      expect($updatedErrorMessages.length).toEqual(4)
    })

    test('should NOT replace existing error message text', () => {
      expect($updatedErrorMessages[0].textContent).toBe('This first message will be replaced')
      expect($updatedErrorMessages[1].textContent).toBe('This second message will be replaced')
      expect($updatedErrorMessages[2].textContent).toBe('This third message will be replaced')
      expect($updatedErrorMessages[3].textContent).toBe('This fourth message will be replaced')
    })
  })

  describe('Hidden form error messages', () => {

    beforeEach(() => {
      document.querySelectorAll('.govuk-error-message')[1].classList.add('govuk-visually-hidden')
      reorderErrorMessages()
      $updatedErrorMessages = document.querySelectorAll('.error-message')
    })

    test('should only display error messages based on the form errors which are NOT visually hidden', () => {
      expect($updatedErrorMessages.length).toEqual(2)
    })

    test('should replace error messages with form errors based on position within form which are NOT visually hidden', () => {
      expect($updatedErrorMessages[0].textContent).toBe('This is the first new error message')
      expect($updatedErrorMessages[1].textContent).toBe('This is the third new error message')
    })

    test('should create target links to the relevant form error message', () => {
      expect($updatedErrorMessages[0].getAttribute('href')).toBe('#firstInput-error')
      expect($updatedErrorMessages[1].getAttribute('href')).toBe('#thirdInput-error')
    })
  })
})
