'use strict'

import { initProgressiveRadioButtons } from './progressiveRadioButtons'

describe('Progressive radio buttons component', () => {

  beforeEach(() => {
    document.body.innerHTML =
      '<div class="govuk-form-group" data-module="progressive-radios">' +
      '  <fieldset class="govuk-fieldset">' +
      '    <legend class="govuk-fieldset__legend govuk-label">' +
      '      <span>Radio buttons legend</span>' +
      '    </legend>' +
      '    <div class="govuk-radios">' +
      '      <div class="govuk-radios__item">' +
      '        <input id="radioItems_one" name="radioItems" type="radio" value="one" class="govuk-radios__input" aria-controls="qa-content-progressive" aria-expanded="false" />' +
      '        <label for="radioItems_one" class="govuk-label govuk-radios__label">Radio 1</label>' +
      '      </div>' +
      '      <div class="govuk-radios__item">' +
      '        <input id="radioItems_two" name="radioItems" type="radio" value="two" class="govuk-radios__input" aria-controls="qa-content-progressive" aria-expanded="false" />' +
      '        <label for="radioItems_two" class="govuk-label govuk-radios__label">Radio 2</label>' +
      '      </div>' +
      '    </div>' +
      '  </fieldset>' +
      '  <div id="qa-content-progressive" class="govuk-radios__conditional govuk-radios__conditional--hidden">' +
      '    <p id="qa-nested-content">Progressive content one</p>' +
      '  </div>' +
      '</div>'

    initProgressiveRadioButtons()
  })

  test('should render with the progressive content hidden', () => {
    expect(document.getElementById('qa-content-progressive').classList.contains('govuk-radios__conditional--hidden')).toBeTruthy()
  })

  test('should display progressive content when the relevant radio button is clicked', () => {
    const radioButton = document.getElementById('radioItems_one')
    radioButton.click();

    expect(radioButton.getAttribute('aria-expanded')).toBe('true')
    expect(document.getElementById('qa-content-progressive').classList.contains('govuk-radios__conditional--hidden')).toBeFalsy()
  })
})
