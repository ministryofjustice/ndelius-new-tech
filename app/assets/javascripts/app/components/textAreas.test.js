import tinymce from 'tinymce/tinymce'

/**
 * @TODO: We cannot test with tinymce as jsdom have not implemented range methods
 *        It will have to be mocked correctly...?
 * @see: https://github.com/tinymce/tinymce-react/issues/61
 */

jest.mock('tinymce/tinymce')
jest.mock('../components/saveIcon')
jest.mock('../utilities/xhrPromisify', () => ({
  promisifyXMLHttpRequest: jest.fn().mockImplementation(() => {
    return new Promise((resolve) => {
      process.nextTick(() => resolve())
    })
  })
}))

window.matchMedia = jest.fn().mockImplementation(query => {
  return {
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  }
})

import { initTextAreas } from './textAreas'

describe('textarea component', () => {

  beforeEach(() => {
    document.body.innerHTML =
      '<form id="ndForm" action="/some/form/url">' +
      '  <div class="govuk-form-group">' +
      '    <div class="testTextArea-autosave_error">Auto save error</div>' +
      '    <label class="govuk-label" for="testTextArea">Test text area</label>' +
      '    <textarea id="testTextArea" class="govuk-textarea govuk-visually-hidden" data-limit="2000"></textarea>' +
      '    <div id="testTextArea-tinymce" class="moj-rich-text-editor"></div>' +
      '    <input id="jumpNumber" value="2" type="hidden" />' +
      '    <div id="testTextArea-countHolder" class="govuk-visually-hidden">' +
      '      <div id="testTextArea-count"></div>' +
      '    </div>' +
      '  </div>' +
      '</form>'

    initTextAreas()
  })

  it('should initialise the tinymce instance', () => {
    expect(tinymce.init).toHaveBeenCalled()
  })
})
