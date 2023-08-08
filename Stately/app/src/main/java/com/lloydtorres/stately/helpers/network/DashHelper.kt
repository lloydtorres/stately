/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lloydtorres.stately.helpers.network

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.lloydtorres.stately.R

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


 */ /**
 * Created by Lloyd on 2016-02-01.
 *
 * DashHelper is a singleton class for network purposes.
 * Used for getting instances of the Volley request queue, imageloaders, etc.
 */
class DashHelper private constructor(private var context: Context) {
    private var mContext: Context
    private var mRequestQueue: RequestQueue?
    private var rateLimit: Int
    private var lastReset: Long
    private var numCalls: Int

    /**
     * Private constructor.
     * @param c App context
     */
    init {
        mContext = context
        mRequestQueue = requestQueue
        rateLimit = DEFAULT_RATE_LIMIT
        lastReset = System.currentTimeMillis()
        numCalls = 0
    }

    /**
     * Creates a new RequestQueue if it doesn't already exist, then returns it.
     * @return The Volley RequestQueue
     */
    val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(mContext.applicationContext)
            }
            return mRequestQueue!!
        }

    /**
     * Adds a request to the Volley queue.
     * @param req The Volley request.
     */
    @Synchronized
    fun <T> addRequest(req: Request<T>): Boolean {
        if (isNotLockout) {
            req.retryPolicy = RETRY_POLICY
            requestQueue.add(req)
            return true
        }
        return false
    }

    @Synchronized
    fun setRateLimit(limit: Int) {
        rateLimit = maxOf(limit - RATE_LIMIT_BUFFER, 1)
    }

    @Synchronized
    fun setRemainingCalls(remaining: Int) {
        numCalls = rateLimit - remaining
    }

    @Synchronized
    fun setNextReset(nextResetInSeconds: Int) {
        lastReset = System.currentTimeMillis() + nextResetInSeconds * 1000L
    }

    /**
     * Determines if adding a request will violate rate limits.
     * @return True if request can be granted, false otherwise
     */
    private val isNotLockout: Boolean
        private get() {
            // Reset the call counter if more than 30 seconds have passed
            val curTime = System.currentTimeMillis()
            if (curTime - lastReset >= RATE_LIMIT_RESET_IN_MS) {
                numCalls = 0
                lastReset = curTime
            }
            return if (numCalls < rateLimit) {
                numCalls++
                true
            } else {
                false
            }
        }

    fun loadImage(url: String?, target: ImageView) {
        target.load(url, getImageLoader(target.context)) {
            crossfade(true)
            error(R.drawable.gray)
        }
        numCalls++
    }

    fun loadImageWithoutPlaceHolder(drawable: Int, target: ImageView) {
        target.load(drawable) {
            crossfade(true)
        }
    }

    fun getImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context).components {
            add(SvgDecoder.Factory())
        }.build()
    }

    companion object {
        private const val TIMEOUT_IN_MS = 120000
        private const val RATE_LIMIT_RESET_IN_MS = 30000
        private const val RATE_LIMIT_BUFFER = 5
        private const val DEFAULT_RATE_LIMIT = 50 - RATE_LIMIT_BUFFER

        private val RETRY_POLICY = DefaultRetryPolicy(TIMEOUT_IN_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        private var mDashie: DashHelper? = null

        @JvmStatic
        @Synchronized
        fun getInstance(c: Context): DashHelper? {
            if (mDashie == null) {
                mDashie = DashHelper(c)
            }
            return mDashie
        }
    }
}