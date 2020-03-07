$package("com.kidscademy");

com.kidscademy.ImageEditor = class extends js.dom.Element {
    constructor(ownerDoc, node) {
        super(ownerDoc, node);

        this._aspectRatio = 1;

        this._image = null;

        this._transformsCount = 0;

		/**
		 * Image editor has two sections: info view - both file and crop area info, and image preview with 
		 * crop mask. It control visibility of its children.
		 * @type {js.dom.Element}
		 */
        this._editor = this.getByCssClass("editor");

		/**
		 * File info view display information about image file. It is a child of {@link #_imageEditor}.
		 * @type {js.dom.Element}
		 */
        this._fileInfoView = this.getByCssClass("file-info");

		/**
		 * Crop info view display information about crop area, e.g. position and dimensions. It is a child of {@link #_imageEditor}.
		 * @type {js.dom.Element}
		 */
        this._cropInfoView = this.getByCssClass("crop-info");

        /**
         * Image element that display the actual image preview.
         * @type {js.dom.Image}
         */
        this._previewImage = this.getByCss(".preview img");
        this._previewImage.on("load", this._onPreviewImageLoad, this);

		/**
		 * Crop mask is used by user to select crop area. It is overlayed on {@link #_previewImage}.
		 * @type {com.kidscademy.CropMask}
		 */
        this._cropMask = this.getByClass(com.kidscademy.CropMask);

        this._actions = this.getByClass(com.kidscademy.Actions).bind(this);
        this._actions.showOnly("add");
        // register event for hidden input of type file to trigger image loading from host OS
        this.getByName("file-upload").on("change", this._onFileUpload, this);

        this._events = this.getCustomEvents();
        this._events.register("open", "close", "upload", "link", "change", "remove");
    }

    config(config) {
        this._aspectRatio = config.aspectRatio;
    }

    open(image, callback) {
        // this._actions.show("remove");
        this._callback = callback;
        this._image = image;
        this._previewImage.setSrc(image.src);
        this._events.fire("open");
    }

    _onAdd() {
        this._actions.show("upload", "link", "close");
        this._editor.hide();
        this._events.fire("open");
    }

    _onUpload() {
    }

    _onFileUpload(ev) {
        const handler = {
            file: ev.target._node.files[0],
            callback: image => {
                this._image = image;
                this._previewImage.reload(image.src);
            }
        }
        this._events.fire("upload", handler);
    }

    _onLink() {
        const callback = image => {
            this._image = image;
            this._previewImage.reload(image.src);
        };
        this._events.fire("link", callback);
    }

    _onCrop() {
        this._cropMask.open({
            width: this._previewImage._node.width,
            height: this._previewImage._node.height,
            naturalWidth: this._previewImage._node.naturalWidth,
            naturalHeight: this._previewImage._node.naturalHeight,
            aspectRatio: this._aspectRatio
        }, this._onCropUpdate, this);
    }

	/**
	 * Invoked in real time when user changes crop area. It gets data about crop area position and dimensions
	 * and display it on {@link #_cropInfoView}.
	 * 
	 * @param {Object} cropInfo crop area info. 
	 */
    _onCropUpdate(cropInfo) {
        this._cropInfoView.setObject(cropInfo);
    }

    _onTrim() {
        ImageService.trimImage(this._image, this._onProcessingDone, this);
    }

    _onFlop() {
        ImageService.flopImage(this._image, this._onProcessingDone, this);
    }

    _onFlip() {
        ImageService.flipImage(this._image, this._onProcessingDone, this);
    }

    _onRotateLeft() {
        ImageService.rotateImageLeft(this._image, this._onProcessingDone, this);
    }

    _onRotateRight() {
        ImageService.rotateImageRight(this._image, this._onProcessingDone, this);
    }

	/**
	 * Commit current operation. Done button action depends on context. If current operation is <code>duplicate</code> this
	 * handler execute it on server.
	 */
    _onDone() {
        switch (this._actions.getPreviousAction()) {
            case "crop":
                const crop = this._cropMask.getCropArea();
                this._cropMask.hide();
                ImageService.cropImage(this._image, crop.cx, crop.cy, crop.x, crop.y, this._onProcessingDone, this);
                break;

            default:
                ImageService.commitImage(this._image, image => {
                    this._image = image;
                    this._closeImageEditor();
                    //this._callback(image);
                });
        }
    }

    _onUndo() {
        ImageService.undoImage(this._image, image => {
            --this._transformsCount;
            this._image = image;
            this._previewImage.reload(image.src);
        });
    }

    _onClose() {
        if (this._transformsCount === 0) {
            this._closeImageEditor();
            return;
        }
        js.ua.System.confirm("@string/confirm-image-rollback", answer => {
            if (answer === true) {
                ImageService.rollbackImage(this._image, this._closeImageEditor, this);
            }
        });
    }

    _onRemove() {
        js.ua.System.confirm("@string/confirm-image-remove", answer => {
            if (answer === true) {
                this._events.fire("remove", this._image);
                this._closeImageEditor();
            }
        });
    }

    // --------------------------------------------------------------------------------------------

    _closeImageEditor() {
        this._actions.showOnly("add");
        this._editor.hide();
        this._events.fire("close", this._image);
        this._image = null;
    }

	/**
	 * Callback invoked when server image processing is complete.
	 * 
	 * @param {Object} image image returned by server.
	 */
    _onProcessingDone(image) {
        ++this._transformsCount;
        this._image = image;
        this._previewImage.reload(image.src);
        this._events.fire("change", image);
    }

    _onPreviewImageLoad(ev) {
        this._transformsCount = 0;
        this._actions.showAll().hide("add", "upload", "link");
        this._cropMask.hide();
        this._editor.show();
        this._fileInfoView.setObject(this._image);
    }

	/**
	 * Class string representation.
	 * @return {String} this class string representation.
	 */
    toString() {
        return "com.kidscademy.ImageEditor";
    }
};
