/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tobinornottobin2.ObjectDetection.ObjectDetection;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;


/** A {@link TextureView} that can be adjusted to a specified aspect ratio. */
public class AutoFitTextureView  {
    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public AutoFitTextureView(final Context context) {
        this(context, null);
    }

    public AutoFitTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super();
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that is,
     * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }

    private void requestLayout() {
    }



    public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = View.MeasureSpec.getSize(widthMeasureSpec);
        final int height = View.MeasureSpec.getSize(heightMeasureSpec);
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
            }
        }
    }

    private void setMeasuredDimension(int width, int height) {
    }




    public  int getHeight() {
        return 0;
    }

    public void setSurfaceTextureListener(TextureView.SurfaceTextureListener surfaceTextureListener) {
    }

    public SurfaceTexture getSurfaceTexture() {
        return null;
    }

    public void setTransform(Matrix matrix) {
    }

    public boolean isAvailable() {
        return false;
    }

    public int getWidth() {
        return 0;
    }
}
