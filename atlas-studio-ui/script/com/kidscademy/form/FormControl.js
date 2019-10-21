$package("com.kidscademy.form");

/**
 * Base control for atlas objects form.
 */
com.kidscademy.form.FormControl = class extends js.dom.Control {
    constructor(ownerDoc, node) {
        super(ownerDoc, node);

		/**
		 * Parent form page.
		 * @type {com.kidscademy.form.FormPage}
		 */
        this._formPage = null;
    }

    /**
     * Form control life cycle hook. Invoked after parent form created but before atlas object loaded.
     */
    onCreate(formPage) {
        this._formPage = formPage;
    }

    /**
     * Form control life cycle hook. Invoked after atlas object was loaded and this control value updated.
     */
    onStart() {

    }

    _setDirty() {
        this._formPage._dirty = true;
    }

	/**
	 * Class string representation.
	 * 
	 * @return this class string representation.
	 */
    toString() {
        return "com.kidscademy.form.FormControl";
    }
};