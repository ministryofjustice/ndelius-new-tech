(function ($) {

    'use strict';

    $(function () {

        // Show/hide content
        var showHideContent = new GOVUK.ShowHideContent(),
            saveTimer;

        showHideContent.init();

        // Stick at top when scrolling
        GOVUK.stickAtTopWhenScrolling.init();

        /**
         *
         * @param parent
         * @param child
         */
        function showHint(parent, child) {
            child.hasClass('js-hidden') ? child.removeClass('js-hidden') : child.addClass('js-hidden');

            if (child.hasClass('js-hidden')) {
                parent.removeClass('active');
                parent.attr('aria-expanded', 'false');
                child.attr('aria-hidden', 'true');
            } else {
                parent.addClass('active');
                parent.attr('aria-expanded', 'true');
                child.attr('aria-hidden', 'false');
            }
        }

        /**
         * Start save icon
         * @param elem
         */
        function startSaveIcon(elem) {
            var saveIcon = $('#' + elem.attr('id') + '_save'),
                spinner = $('.spinner', saveIcon);

            saveIcon.removeClass('js-hidden');
            spinner.removeClass('error');
            spinner.addClass('active');
        }

        /**
         * End save icon
         * @param elem
         * @param error
         */
        function endSaveIcon(elem, error) {
            var saveIcon = $('#' + elem.attr('id') + '_save'),
                spinner = $('.spinner', saveIcon);

            spinner.removeClass('active');
            error ? spinner.addClass('error') : spinner.removeClass('error');
        }

        /**
         * Save
         */
        function saveProgress(elem, stop) {

            var form = $('form');

            if (saveTimer) {
                clearTimeout(saveTimer);
            }

            startSaveIcon(elem);

            if (form.length) {

                var data = _.map(form.serializeArray(), function (entry) {
                    if (entry.name === 'jumpNumber') {
                        entry.value = 0;
                    }
                    return entry;
                });

                $.ajax({
                    type: 'POST',
                    url: form.attr('action'),
                    data: data,
                    error: function () {
                        endSaveIcon(elem, true);
                    },
                    success: function () {
                        setTimeout(function () {
                            endSaveIcon(elem);
                        }, 1500);
                        if (!stop) {
                            saveTimer = setTimeout(function () {
                                saveProgress(elem);
                            }, 15000);
                        }
                    }
                });
            }
        }

        /**
         * Textarea elements
         */
        $('textarea').focus(function () {
            saveTimer = setTimeout(function () {
                saveProgress($(this));
            }.bind(this), 5000);
        }).keyup(function () {
            var textArea = $(this),
                limit = textArea.data('limit'),
                current = textArea.val().length,
                messageHolder = $('#' + textArea.attr('id') + '-countHolder'),
                messageTarget = $('#' + textArea.attr('id') + '-count');

            if (limit && current > 0) {
                messageHolder.removeClass('js-hidden');
                messageTarget.text(limit + ' recommended characters, you have used ' + current);
            } else {
                messageHolder.addClass('js-hidden');
            }

        }).blur(function () {
            saveProgress($(this), true);
        });

        /**
         * Navigation items
         */
        $('.nav-item').click(function (e) {

            e.preventDefault();

            var target = $(this).data('target');
            if (target && !$(this).hasClass('active')) {
                $('#jumpNumber').val(target);
                $('form').submit();
            }
        });

        /**
         * Feedback link - change form action and submit
         */
        $('.feedback-link').click(function (e) {
            e.preventDefault();

            var form = $('form');
            form.attr('action', form.attr('action') + '/feedback').submit();
        });

        /**
         * Ensure jumpNumber is cleared if next after clicking browser back button
         */
        $('#nextButton').click(function () {
            $('#jumpNumber').val('');
        });

        /**
         * Save and exit
         */
        $('#exitLink').click(function (e) {
            e.preventDefault();
            $('#jumpNumber').val(0);
            $('form').submit();
        });

        /**
         *
         */
        $('a.expand-content').each(function (i, elem) {
            var parent = $(this),
                child = $('#' + elem.getAttribute('data-target'));
            parent.attr('aria-controls', elem.getAttribute('data-target'));
            parent.click(function () {
                showHint(parent, child)
            });
        });

        // Progressive enhancement for browsers > IE8
        if (!$('html').is('.lte-ie8')) {
            // Autosize all Textarea elements (does not support IE8).
            autosize(document.querySelectorAll('textarea'));

            // Autocomplete
            var autoComplete = document.querySelector('.auto-complete');
            if (autoComplete) {
                console.info(autoComplete.id);
                accessibleAutocomplete.enhanceSelectElement({
                    selectElement: autoComplete,
                    name: autoComplete.id,
                    defaultValue: '',
                    required: true
                });
            }

            // Date picker
            $('.date-picker').datepicker({
                dateFormat: 'dd/mm/yy'
            }).parent().addClass('date-wrapper');
        }

    });

})(window.jQuery);
