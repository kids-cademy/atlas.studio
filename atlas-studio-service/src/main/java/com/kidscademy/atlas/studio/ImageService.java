package com.kidscademy.atlas.studio;

import java.io.IOException;

import com.kidscademy.atlas.studio.model.AtlasItem;
import com.kidscademy.atlas.studio.model.Image;

import js.rmi.BusinessException;
import js.tiny.container.annotation.Public;
import js.tiny.container.annotation.Service;
import js.tiny.container.http.form.Form;

@Service
@Public
public interface ImageService {
    Image uploadImage(Form form) throws IOException, BusinessException;

    Image uploadImageBySource(Form form) throws IOException, BusinessException;

    Image cloneImageToIcon(AtlasItem object, Image image) throws IOException;

    void removeImage(AtlasItem object, Image image) throws IOException;

    
    
    Image trimImage(Image image) throws IOException;

    Image flopImage(Image image) throws IOException;

    Image flipImage(Image image) throws IOException;

    Image rotateImageLeft(Image image) throws IOException;

    Image rotateImageRight(Image image) throws IOException;

    Image cropImage(Image image, int width, int height, int xoffset, int yoffset) throws IOException;

    Image undoImage(Image image) throws IOException;

    Image commitImage(Image image) throws IOException;

    void rollbackImage(Image image) throws IOException;
}