package com.lloydtorres.stately.helpers;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.URLSpan;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-01-20.
 */
public class URLSpanNoUnderline extends URLSpan {
    private Context context;

    public URLSpanNoUnderline(Context c, String url) {
        super(url);
        context = c;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }
}
