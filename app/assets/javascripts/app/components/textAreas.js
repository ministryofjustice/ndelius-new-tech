import tinymce from 'tinymce/tinymce'
import 'tinymce/themes/silver/theme'
import 'tinymce/plugins/autoresize'
import 'tinymce/plugins/help'
import 'tinymce/plugins/lists'
import 'tinymce/plugins/paste'
import 'tinymce/plugins/spellchecker'

import { autoSaveProgress } from '../helpers/saveProgressHelper'
import { debounce } from '../utilities/debounce'
import { nodeListForEach } from '../utilities/nodeListForEach'
import { trackEvent } from '../../helpers/analyticsHelper'

/**
 *
 * @param $editor
 */
function attachToolbar ($editor) {
  const $toolbar = document.getElementById('tinymce-toolbar')
  const $container = $editor.getElement().parentElement
  $container.appendChild($toolbar)
}

/**
 *
 * @param $editor
 */
function addPlaceholder ($editor) {
  if (!$editor.getContent({ format: 'text' }).trim().length) {
    $editor.getElement().classList.add('tox-tinymce--placeholder')
  }
}

/**
 *
 * @param $editor
 */
function removePlaceholder ($editor) {
  $editor.getElement().classList.remove('tox-tinymce--placeholder')
}

/**
 *
 * @param $editor
 */
function updateFormElement ($editor) {
  document.getElementById($editor.getElement().dataset.id).value = $editor.getContent()
}

/**
 *
 * @param $editor
 */
function updateTextLimits ($editor) {
  const elementId = $editor.getElement().dataset.id
  const messageHolder = document.getElementById(`${ elementId }-countHolder`)
  const messageTarget = document.getElementById(`${ elementId }-count`)

  if (!messageHolder || !messageTarget) {
    return
  }

  const limit = $editor.getElement().dataset.limit
  const current = $editor.getContent({ format: 'text' }).trim().length

  if (limit && current > 0) {
    messageHolder.classList.remove('govuk-visually-hidden')
    messageTarget.innerText = `${ limit } recommended characters, you have used ${ current }`
  } else {
    messageHolder.classList.add('govuk-visually-hidden')
  }
}

/**
 *
 * @param $editor
 */
function updateTooltips ($editor) {
  const isMac = navigator.platform.toUpperCase().indexOf('MAC') !== -1
  const $control = isMac ? '⌘' : 'Ctrl-'
  const $shift = isMac ? '⇧' : 'Shift-'

  nodeListForEach($editor.getContainer().querySelectorAll('.tox-tbtn'), $module => {
    switch ($module.title) {
      case 'Undo':
        $module.title = `Undo (${ $control }Z)`
        break
      case 'Redo':
        $module.title = `Redo (${ $control }Y)`
        break
      case 'Bold':
        $module.title = `Bold (${ $control }B)`
        break
      case 'Italic':
        $module.title = `Italic (${ $control }I)`
        break
      case 'Underline':
        $module.title = `Underline (${ $control }U)`
        break
      case 'Align left':
        $module.title = `Align left (${ $control }${ $shift }L)`
        break
      case 'Justify':
        $module.title = `Justify (${ $control }${ $shift }J)`
        break
    }
  })
}

const spellCheckButtonHandlers = new Map()

function addClickHandlerToSpellCheck ($editor) {
  const $spellCheckButton = $editor.getContainer().querySelector('[title="Spellcheck"]')
  if ($spellCheckButton && !spellCheckButtonHandlers.has($editor.id)) {
    $spellCheckButton.addEventListener('click', () =>
      handleSpellCheckClick($spellCheckButton, $editor)
    )
    spellCheckButtonHandlers.set($editor.id, true)
  }
}

function handleSpellCheckClick (spellCheckButton, $editor) {
  if (spellCheckButton.getAttribute("aria-pressed") === "false") {
    $editor.getBody().setAttribute('spellcheck', 'false')
    trackEvent('spellcheck - on', "spellcheck", $editor.id)
  } else {
    $editor.getBody().setAttribute('spellcheck', 'true')
    trackEvent('spellcheck - off', "spellcheck", $editor.id)
  }
}

function enableSpellChecker ($editor) {
  const $spellCheckButton = $editor.getContainer().querySelector('[title="Spellcheck"]')
  if ($spellCheckButton && $spellCheckButton.getAttribute('aria-pressed') === 'false' && $editor.getContent({ format: 'text' }).trim().length) {
    $editor.getBody().setAttribute('spellcheck', 'false')
    $editor.execCommand('mceSpellCheck')
  }
}

function autoClickSpellchecker ($editor) {
  const $spellCheckButton = $editor.getContainer().querySelector('[title="Spellcheck"]')
  if ($spellCheckButton && $spellCheckButton.getAttribute('aria-pressed') === 'true' && $editor.getContent({ format: 'text' }).trim().length) {
    $editor.execCommand('mceSpellCheck')
    $editor.execCommand('mceSpellCheck')
  }
}

/**
 *
 */
const initTextAreas = () => {

  const localPath = window.localPath || '/'

  tinymce.init({
    menubar: false,
    inline: true,
    browser_spellcheck: true,
    allow_conditional_comments: true,
    fixed_toolbar_container: '#tinymce-toolbar',
    selector: '.moj-rich-text-editor:not(.moj-textarea--classic)',
    plugins: 'autoresize lists paste help spellchecker',
    width: '100%',
    min_height: 145,
    valid_elements: 'p,p[style],span[style],ul,ol,li,li[style],strong/b,em/i,br',
    toolbar: 'undo redo | bold italic underline | alignleft alignjustify | numlist bullist | spellchecker',
    valid_classes: {
      'p': '',
      'span': ''
    },
    valid_styles: {
      'p': 'text-align',
      'li': 'text-align',
      'span': 'text-decoration'
    },
    paste_enable_default_filters: 'false',
    paste_word_valid_elements: 'p,b,i,strong,em,ol,ul,li',
    document_base_url: `${ localPath.substr(0, localPath.indexOf('/') + 1) }`,
    skin_url: 'assets/skins/ui/oxide',
    spellchecker_languages: 'English=en',
    setup: $editor => {
      $editor.on('init', () => {
        addPlaceholder($editor)
      })
      $editor.on('focus', () => {
        attachToolbar($editor)
        updateTooltips($editor)
        removePlaceholder($editor)
      })
      $editor.on('focus', debounce(() => {
        addClickHandlerToSpellCheck($editor)
        //enableSpellChecker($editor)
      }), 50)
      $editor.on('blur', () => {
        addPlaceholder($editor)
        updateFormElement($editor)
        autoSaveProgress($editor.getElement().dataset.id)
      })
      $editor.on('keyup', debounce(() => {
        updateFormElement($editor)
        autoSaveProgress($editor.getElement().dataset.id)
      }, 5000))
      $editor.on('keyup', debounce(() => {
        autoClickSpellchecker($editor)
      }, 100))
      $editor.on('input', () => {
        updateTextLimits($editor)
      })
    },
    spellchecker_callback: function (method, text, success, failure) {
      if (method === 'spellcheck') {
        tinymce.util.JSONRequest.sendRPC({
          url: '/spellcheck',
          params: {
            words: text.match(this.getWordCharPattern())
          },
          success: result => {
            success(result)
          },
          error: (error, xhr) => {
            failure('Spellcheck error:' + xhr.status)
          }
        })
      } else {
        failure('Unsupported spellcheck method')
      }
    }
  })
}

export {
  initTextAreas
}
