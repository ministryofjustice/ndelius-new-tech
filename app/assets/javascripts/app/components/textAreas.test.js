import tinymce from 'tinymce/tinymce'
import { initTextAreas } from './textAreas'

/**
 * @TODO: We cannot test with tinymce as jsdom have not implemented range methods
 *        It will have to be mocked correctly...?
 * @see: https://github.com/tinymce/tinymce-react/issues/61
 */

jest.mock('tinymce/tinymce')
jest.mock('tinymce/icons/default/icons', () => ({}));
jest.mock('tinymce/plugins/help', () => ({}));
jest.mock('tinymce/plugins/lists', () => ({}));
jest.mock('tinymce/plugins/paste', () => ({}));
jest.mock('tinymce/plugins/spellchecker', () => ({}));

jest.mock('../components/saveIcon')
jest.mock('../utilities/xhrPromisify', () => ({
  promisifyXMLHttpRequest: jest.fn().mockImplementation(() => {
    return new Promise((resolve) => {
      process.nextTick(() => resolve())
    })
  })
}))

describe('textarea component', () => {

  beforeEach(() => {
    document.body.innerHTML =
      '<form id="ndForm" action="/some/form/url">' +
      '  <div class="govuk-form-group">' +
      '    <div class="testTextArea-autosave_error">Auto save error</div>' +
      '    <label class="govuk-label" for="testTextArea">Test text area</label>' +
      '    <textarea id="testTextArea" class="govuk-textarea govuk-visually-hidden" data-limit="2000"/>' +
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
