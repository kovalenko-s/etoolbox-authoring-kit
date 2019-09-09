/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 1.0.0
 *
 * CQ Coral3 based FileUpload accessor
 * */
(function ($, ns) {
    const SELECTOR = '.cq-FileUpload.coral3-FileUpload';
    const FILEUPLOAD_FILE_REFERENCE = '[data-cq-fileupload-parameter="filereference"]';

    ns.ElementAccessors.registerAccessor({
        selector: SELECTOR,
        preferableType: 'string',
        get: function ($el) {
            return $el.find(FILEUPLOAD_FILE_REFERENCE).val();
        },
        // set: function ($el, value) {
        //     return $el.find(FILEUPLOAD_FILE_REFERENCE).val(value);
        // },
        required: function ($el, val) {
            $el.attr('data-cq-fileupload-required', val ? 'true' : null);
            ns.ElementAccessors.updateValidity($el, true);
        }
    });

    // CQ Coral3 Fixes
    $(document).on('click', `${SELECTOR} [coral-fileupload-clear]`, function () {
        // Clear handler is not producing change event so handle it manually
        $(this).closest(SELECTOR).trigger('change');
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));