package com.kidscademy.atlas.studio.export;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import js.json.Json;
import js.util.Classes;
import js.util.Params;

public class ZipExportTarget implements ExportTarget {
    private final Json json;

    public ZipExportTarget() {
	this.json = Classes.loadService(Json.class);
    }

    public ZipExportTarget(Json json) {
	this.json = json;
    }

    private ZipOutputStream zip;

    @Override
    public void open(OutputStream stream) {
	Params.notNull(stream, "Output stream");
	zip = new ZipOutputStream(stream);
    }

    @Override
    public void write(Object object, String path) throws IOException {
	ZipEntry entry = new ZipEntry(path);
	zip.putNextEntry(entry);
	zip.write(json.stringify(object).getBytes("UTF-8"));
	zip.closeEntry();
    }

    @Override
    public void write(File file, String path) throws IOException {
	// test null file here in order to simplify invoker logic
	if (file == null || !file.exists()) {
	    return;
	}
	ZipEntry entry = new ZipEntry(path);
	zip.putNextEntry(entry);
	copy(file, zip);
	zip.closeEntry();
    }

    @Override
    public void close() throws IOException {
	zip.close();
    }

    /**
     * Copy source file binary content to output stream keeping it opened.
     * 
     * @param file
     *            binary source file
     * @param outputStream
     *            output stream.
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private static long copy(File file, OutputStream outputStream) throws IOException, IllegalArgumentException {
	InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
	outputStream = new BufferedOutputStream(outputStream);

	long bytes = 0;
	try {
	    byte[] buffer = new byte[4096];
	    int length;
	    while ((length = inputStream.read(buffer)) != -1) {
		bytes += length;
		outputStream.write(buffer, 0, length);
	    }
	} finally {
	    inputStream.close();
	    outputStream.flush();
	}
	return bytes;
    }
}