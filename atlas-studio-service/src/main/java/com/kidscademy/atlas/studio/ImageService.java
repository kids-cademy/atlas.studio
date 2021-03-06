package com.kidscademy.atlas.studio;

import java.io.IOException;

import com.kidscademy.atlas.studio.model.Gravity;
import com.kidscademy.atlas.studio.model.Image;
import com.kidscademy.atlas.studio.model.RotateDirection;

import js.tiny.container.annotation.Service;

@Service
public interface ImageService {
    Image trimImage(Image image) throws IOException;

    Image canvasSize(Image image, int width, int height, Gravity gravity) throws IOException;

    Image flopImage(Image image) throws IOException;

    Image flipImage(Image image) throws IOException;

    Image rotateImage(Image image, RotateDirection direction, float angle) throws IOException;

    Image cropRectangleImage(Image image, int width, int height, int xoffset, int yoffset) throws IOException;

    Image cropCircleImage(Image image, int width, int height, int xoffset, int yoffset, String borderColor,
	    int borderWidth) throws IOException;

    Image adjustBrightnessContrast(Image image, int brightness, int contrast) throws IOException;

    Image undoImage(Image image) throws IOException;

    Image commitImage(Image image) throws IOException;

    void rollbackImage(Image image) throws IOException;
}
