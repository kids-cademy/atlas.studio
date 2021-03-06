$package("com.kidscademy.release");

com.kidscademy.release.GraphicAssets = class extends js.dom.Control {
    constructor(ownerDoc, node) {
        super(ownerDoc, node);

        this._iconView = this.getParent().getByName("images.icon");
        this._featureView = this.getParent().getByName("images.feature");
        this._coverView = this.getParent().getByName("images.cover");

        this._editor = this.getByClass(com.kidscademy.FormData);

        this._actions = this.getByClass(com.kidscademy.Actions).showOnly("edit").bind(this);
        // register event for hidden input of type file to trigger image loading from host OS
        this.getByName("upload-file").on("change", this._onUploadFile, this);
    }

    setValue(graphicsBackground) {
        this._editor.setObject({ background: graphicsBackground });
    }

    getValue() {
        return this._editor.getObject().background;
    }

    isValid() {
        return this._editor.isValid();
    }

    _onEdit() {
        this._actions.showAll().hide("edit");
        this._editor.show();
    }

    _onUpload(ev) {
        if (!this._editor.isValid()) {
            ev.halt();
        }
    }

    _onUploadFile(ev) {
        const release = WinMain.page.getRelease();

        const formData = this._editor.getFormData();
        formData.append("image-kind", "RELEASE");
        formData.append("object-id", release.id);
        formData.append("media-file", ev.target._node.files[0]);

        ReleaseService.uploadReleaseImage(formData, image => this._updateUserInterface());
    }

    _onLink() {

    }

    _onDone() {
        const release = WinMain.page.getRelease();
        ReleaseService.updateReleaseGraphics(release.id, this._editor.getObject().background, () => this._updateUserInterface());
    }

    _onClose() {
        this._actions.showOnly("edit");
        this._editor.hide();
    }

    _onImageEditorLink() {
        if (!this._metaForm.isValid()) {
            return;
        }
        const release = WinMain.page.getRelease();

        const formData = this._metaForm.getFormData();
        formData.append("image-kind", "RELEASE");
        formData.append("object-id", release.id);

        AtlasService.uploadImageBySource(formData, image => this._updateUserInterface());
    }

    _updateUserInterface() {
        this._iconView.reload();
        this._featureView.reload();
        this._coverView.reload();
        this._onClose();
    }

    toString() {
        return "com.kidscademy.release.GraphicAssets";
    }
};
