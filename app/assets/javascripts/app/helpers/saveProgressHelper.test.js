import {promisifyXMLHttpRequest} from '../utilities/xhrPromisify'
import {autoSaveProgress} from './saveProgressHelper'
import {startSaveIcon} from '../components/saveIcon'
import {formWithZeroJumpNumber} from '../utilities/formWithZeroJumpNumber'

jest.mock('../components/saveIcon')

jest.mock('../utilities/xhrPromisify', () => ({
  promisifyXMLHttpRequest: jest.fn().mockImplementation(() => {
    return new Promise(resolve => resolve())
  })
}))

describe('save progress helper component', () => {

  beforeEach(() => {
    document.body.innerHTML =
      '<form id="ndForm" action="/some/form/url">' +
      '  <div class="govuk-form-group">' +
      '    <div class="testTextArea-autosave_error">Auto save error</div>' +
      '    <label class="govuk-label" for="testTextArea">Test text area</label>' +
      '    <textarea id="testTextArea" class="govuk-textarea"></textarea>' +
      '    <input id="jumpNumber" value="2" type="hidden" />' +
      '  </div>' +
      '</form>'
  })

  it('should call the startSaveIcon method and XHR promisify method with form data', async () => {
    const formData = formWithZeroJumpNumber(document.getElementById('ndForm'))
    autoSaveProgress({ id: 'testTextArea' })
    expect(startSaveIcon).toHaveBeenCalled()
    expect(promisifyXMLHttpRequest).toBeCalledWith({ 'body': formData, 'method': 'POST', 'url': '/some/form/url/save' })
  })
})
