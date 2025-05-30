import { nodeListForEach } from '../utilities/nodeListForEach'

const initProgressiveRadioButtons = () => {

  nodeListForEach(document.querySelectorAll('[data-module="progressive-radios"]'), $module => {

    const $inputs = $module.querySelectorAll('input[type="radio"]')
    nodeListForEach($inputs, $input => {
      let controls = $input.getAttribute('aria-controls')
      controls = controls ? controls.substr(0, controls.lastIndexOf('-')) + '-progressive' : ''

      // Check if input controls anything and content exists.
      if (!controls || !$module.querySelector('#' + controls)) {
        return
      }

      $input.setAttribute('aria-controls', controls)
      $input.removeAttribute('_')
      $input.addEventListener('click', () => {
        activate(controls)
      })

      if ($input.checked) {
        activate(controls)
      }
    })

    function activate (id) {
      $module.querySelector('#' + id).classList.toggle('govuk-radios__conditional--hidden', false)

      nodeListForEach($inputs, $input => {
        $input.setAttribute('aria-expanded', $input.checked)
      })
    }
  })
}

export {
  initProgressiveRadioButtons
}
