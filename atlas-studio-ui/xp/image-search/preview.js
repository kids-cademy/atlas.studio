WinMain.on("load", function() {
	this._imageSearch = WinMain.doc.getByClass(com.kidscademy.form.ImageSearch);
	this._imageSearch.open(function(image) {
		alert(image.source)
	});
});