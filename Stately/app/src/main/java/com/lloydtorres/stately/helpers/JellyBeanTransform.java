package com.lloydtorres.stately.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

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

    public JellyBeanTransform(Context c, ImageView img)
    {
        super();
        context = c.getApplicationContext();
        imageView = img;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int bounding = dpToPx(BOUNDING_SIZE);

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

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }
}
