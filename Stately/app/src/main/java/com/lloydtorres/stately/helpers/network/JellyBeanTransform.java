/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.helpers.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.lloydtorres.stately.helpers.RaraHelper;
import com.squareup.picasso.Transformation;

/**
 * Created by Lloyd on 2016-02-11.
 * Helper class for adjusting the view bounds of images for Android <= 4.2.
 * Solution for bitmap transform taken from: http://stackoverflow.com/a/8233084
 */
public class JellyBeanTransform implements Transformation {
    private static final int BOUNDING_SIZE = 200;
    private Context context;
    private ImageView imageView;

    public JellyBeanTransform(Context c, ImageView img) {
        super();
        context = c.getApplicationContext();
        imageView = img;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int bounding = RaraHelper.dpToPx(context, BOUNDING_SIZE);

        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap scaledBitmap = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        if (scaledBitmap != source) {
            source.recycle();
            source = null;
        }
        return scaledBitmap;
    }

    @Override
    public String key() {
        return "JellyBeanTransform";
    }
}
