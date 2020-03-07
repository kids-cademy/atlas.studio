$package("com.kidscademy.page");

/**
 * FormPage class.
 * 
 * @author Iulian Rotaru
 */
com.kidscademy.page.FormPage = class extends com.kidscademy.page.Page {
	/**
	 * Construct an instance of FormPage class.
	 */
	constructor() {
		super();

		this.CSS_INVALID = js.dom.Control.prototype.CSS_INVALID;


		// current selected collection and object ID are stored on global context
		this._collection = this.getContextAttr("collection");
		this._objectId = Number(this.getContextAttr("objectId"));

		// TODO: temporar solution
		this._objectId = Number(WinMain.url.parameters.id);

		/**
		 * Flag true when a form control was changed. Used to signal user changes when leaving the page.
		 * @type {Boolean}
		 */
		this._dirty = false;
		WinMain.on("pre-unload", this._onPreUnload, this);

		this._form = this.getByClass(com.kidscademy.Form);
		this._form.on("input", ev => { this._dirty = true });
		this._sidebar = this.getByCss(".side-bar .header");

		const actions = this.getByCss(".side-bar .actions");
		actions.on(this, {
			"&preview": this._onPreview,
			"&save": this._onSave,
			"&remove": this._onRemove
		});

		const quickLinks = this.getByCssClass("quick-links");
		quickLinks.on("click", this._onQuickLinks, this);

		this._taxonomyControl = this.getByClass(com.kidscademy.form.TaxonomyControl);
		this._definitionControl = this.getByClass(com.kidscademy.form.DefinitionControl);
		this._descriptionControl = this.getByClass(com.kidscademy.form.DescriptionControl);
		this._graphicAssets = this.getByClass(com.kidscademy.form.GraphicAssets);
		this._audioAssets = this.getByClass(com.kidscademy.form.AudioAssets);
		this._factsControl = this.getByClass(com.kidscademy.form.FactsControl);
		this._featuresControl = this.getByClass(com.kidscademy.form.FeaturesControl);
		this._spreadingControl = this.getByClass(com.kidscademy.form.SpreadingControl);
		this._relatedControl = this.getByClass(com.kidscademy.form.RelatedControl);
		this._linksControl = this.getByClass(com.kidscademy.form.LinksControl);

		this._taxonomyControl.onCreate(this);
		this._definitionControl.onCreate(this);
		this._descriptionControl.onCreate(this);
		this._graphicAssets.onCreate(this);
		this._audioAssets.onCreate(this);
		this._factsControl.onCreate(this);
		this._featuresControl.onCreate(this);
		this._spreadingControl.onCreate(this);
		this._relatedControl.onCreate(this);
		this._linksControl.onCreate(this);

		this._loadObject();
	}

	_onUnload() {
		this._taxonomyControl.onDestroy();
		this._definitionControl.onDestroy();
		this._descriptionControl.onDestroy();
		this._graphicAssets.onDestroy();
		this._audioAssets.onDestroy();
		this._factsControl.onDestroy();
		this._featuresControl.onDestroy();
		this._spreadingControl.onDestroy();
		this._relatedControl.onDestroy();
		this._linksControl.onDestroy();
	}

	getCollection() {
		return this._object.collection;
	}

	getObject() {
		this._form.getObject(this._object);
		return this._object;
	}

	getAtlasItem() {
		this._form.getObject(this._object);
		const atlasItem = {
			id: this._object.id,
			collection: this._collection,
			name: this._object.name
		}
		return atlasItem;
	}

	getLinks(feature) {
		const object = this.getObject();
		if (!object.links) {
			return [];
		}
		return object.links.filter(link => link.features.indexOf(feature) !== -1);
	}

	_loadObject() {
		if (this._objectId !== 0) {
			AtlasService.getAtlasObject(this._objectId, this._onObjectLoaded, this);
		}
		else {
			AtlasService.createAtlasObject(this._collection, this._onObjectLoaded, this);
		}
	}

	_onObjectLoaded(object) {
		this._dirty = false;

		this.findByCss(".quick-links li").removeCssClass(this.CSS_INVALID);
		this._form.reset();

		this._object = object;
		this._form.setObject(object);
		this._sidebar.setObject(object);

		this._taxonomyControl.onStart();
		this._definitionControl.onStart();
		this._descriptionControl.onStart();
		this._graphicAssets.onStart();
		if(!object.collection.flags.audioSample) {
			this._audioAssets.hide();
		}
		this._audioAssets.onStart();
		this._factsControl.onStart();
		this._featuresControl.onStart();
		this._spreadingControl.onStart();
		this._relatedControl.onStart();
		this._linksControl.onStart();
	}

	_getDefinitionControl() {
		return this._definitionControl;
	}

	_getDescriptionControl() {
		return this._descriptionControl;
	}

	// --------------------------------------------------------------------------------------------

	_onPreview() {
		WinMain.assign("@link/reader", { id: this._object.id });
	}

	_onSave(previewCallback) {
		this.findByCss(".quick-links li").removeCssClass(this.CSS_INVALID);
		const updateQuickLink = control => {
			const fieldset = control.getParentByTag("fieldset");
			// by convention quick link class is the fieldset ID
			this.getByCss(`.quick-links [data-name=${fieldset.getAttr("id")}]`).addCssClass(this.CSS_INVALID);
		};

		const object = this.getObject();
		if (this._form.isValid(updateQuickLink)) {
			if (typeof previewCallback === "function") {
				AtlasService.saveAtlasObject(object, previewCallback, this);
			}
			else {
				AtlasService.saveAtlasObject(object, this._onObjectLoaded, this);
			}
		}
	}

	_onQuickLinks(ev) {
		const quickLink = ev.target.getParentByTag("li");
		if (quickLink != null) {
			// by convention quick link name is fieldset ID
			const fieldsetID = quickLink.getAttr("data-name");
			if (fieldsetID != null) {
				this.getById(fieldsetID).scrollIntoView();
				return;
			}
		}
	}

	_onBack() {
		if (this._dirty) {
			js.ua.System.confirm("@string/confirm-object-not-saved", ok => {
				if (ok) {
					WinMain.assign("collection.htm");
				}
			});
			return;
		}
		super._onBack();
	}

	_onPreUnload(ev) {
		if (this._dirty) {
			return "@string/confirm-object-not-saved";
		}
	}

	/**
	 * Class string representation.
	 * 
	 * @return this class string representation.
	 */
	toString() {
		return "com.kidscademy.page.FormPage";
	}
};

WinMain.createPage(com.kidscademy.page.FormPage);
