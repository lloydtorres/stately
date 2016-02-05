package com.lloydtorres.stately.helpers;

/*

                                         __         __----__
                                        /  \__..--'' `-__-__''-_
                                       ( /  \    ``--,,  `-.''''`
                                       | |   `-..__  .,\    `.
                         ___           ( '.  \ ____`\ )`-_    `.
                  ___   (   `.         '\   __/   __\' / `:-.._ \
                 (   `-. `.   `.       .|\_  (   / .-| |'.|    ``'
                  `-.   `-.`.   `.     |' ( ,'\ ( (WW| \W)j
          ..---'''':-`.    `.\   _\   .||  ',  \_\_`/   ``-.
        ,'      .'` .'_`-,   `  (  |  |''.   `.        \__/
       /   _  .'  :' ( ```    __ \  \ |   \ ._:7,______.-'
      | .-'/  : .'  .-`-._   (  `.\  '':   `-\    /
      '`  /  :' : .: .-''>`-. `-. `   | '.    |  (
         -  .' :' : /   / _( `_: `_:. `.  `;.  \  \
         |  | .' : /|  | (___(   (      \   )\  ;  |
        .' .' | | | `. |   \\\`---:.__-'') /  )/   |
        |  |  | | |  | |   ///           |/   '    |
       .' .'  '.'.`; |/ \  /     /             \__/
       |  |    | | |.|   |      /-,_______\       \
      /  / )   | | '|' _/      /     |    |\       \
    .:.-' .'  .' |   )/       /     |     | `--,    \
         /    |  |  / |      |      |     |   /      )
    .__.'    /`  :|/_/|      |      |      | (       |
    `-.___.-`;  / '   |      |      |      |  \      |
           .:_-'      |       \     |       \  `.___/
                       \_______)     \_______)


 */

import android.content.Context;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by Lloyd on 2016-02-01.
 *
 * DashHelper is a singleton class for network purposes.
 * Used for getting instances of the Volley request queue, imageloaders, etc.
 */
public class DashHelper {
    private static final int HALF_MINUTE_MS = 30000;
    private static final int RATE_LIMIT = 45;

    private static DashHelper mDashie;
    private static Context mContext;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOptions;
    private long lastReset;
    private int numCalls;

    /**
     * Private constructor.
     * @param c App context
     */
    private DashHelper(Context c)
    {
        mContext = c;
        mRequestQueue = getRequestQueue();
        mImageLoader = getImageLoader();
        lastReset = System.currentTimeMillis();
        numCalls = 0;
    }

    public static synchronized DashHelper getInstance(Context c)
    {
        if (mDashie == null)
        {
            mDashie = new DashHelper(c);
        }
        return mDashie;
    }

    /**
     * Creates a new RequestQueue if it doesn't already exist, then returns it.
     * @return The Volley RequestQueue
     */
    public RequestQueue getRequestQueue()
    {
        if (mRequestQueue == null)
        {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Adds a request to the Volley queue.
     * @param req The Volley request.
     */
    public synchronized <T> boolean addRequest(Request<T> req)
    {
        if (isNotLockout())
        {
            getRequestQueue().add(req);
            return true;
        }
        return false;
    }

    private boolean isNotLockout()
    {
        // Reset the call counter if more than 30 seconds have passed
        long curTime = System.currentTimeMillis();
        if ((curTime - lastReset) >= HALF_MINUTE_MS)
        {
            numCalls = 0;
            lastReset = curTime;
        }

        if (numCalls < RATE_LIMIT)
        {
            numCalls++;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Creates a new ImageLoader if it doesn't already exist, then returns it.
     * @return The Universal ImageLoader
     */
    public ImageLoader getImageLoader()
    {
        if (mImageLoader == null)
        {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext.getApplicationContext()).build();
            mImageLoader = ImageLoader.getInstance();
            mImageLoader.init(config);

            // Fade image in on finish load
            mImageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();
        }

        return mImageLoader;
    }

    /**
     * Loads a specified image URL into the ImageView
     * @param url The target URL
     * @param imageView The ImageView to put the image in.
     */
    public void displayImage(String url, ImageView imageView)
    {
        getImageLoader().displayImage(url, imageView, mImageOptions);
    }
}
