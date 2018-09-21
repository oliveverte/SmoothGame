package com.olivierpicard.smooth.Graphics;

import android.graphics.Canvas;

/**
 * Dessine l'objet à l'écran, en prenant en compte
 * les modifications fait lors de l'update
 */

public interface IGDrawable {
    GPoint getPosition();
    void setPosition(GPoint position);

    GSize getSize();
    void setSize(GSize size);

    GScene getScene();
    void setScene(GScene scene);

    float getZRotation();
    void setZRotation(float zRotation);

    int getZPosition();
    void setZPosition(int zPosition);

    GRelativeRender getRelativeRender();

    void render(Canvas canvas);
}
