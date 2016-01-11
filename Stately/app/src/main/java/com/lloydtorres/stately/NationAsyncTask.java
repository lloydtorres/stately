package com.lloydtorres.stately;

import android.os.AsyncTask;
import android.util.Log;

import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Lloyd on 2016-01-10.
 */
public class NationAsyncTask extends AsyncTask<Void, Void, Nation> {
    private final String appTag = "com.lloydtorres.stately";

    private NationAsyncResponse callback;
    private String url = "http://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q=name+type+flag+banner";

    public NationAsyncTask(NationAsyncResponse c, String nationName)
    {
        this.callback = c;
        this.url = String.format(this.url,nationName);
    }

    @Override
    protected Nation doInBackground(Void... params) {
        Nation response = null;

        try{
            InputStream input = new URL(url).openStream();
            Persister serializer = new Persister();
            response = serializer.read(Nation.class, input);
            input.close();
        }
        catch (Exception e)
        {
            Log.e(appTag, e.toString());
        }

        return response;
    }

    @Override
    protected void onPostExecute(Nation n)
    {
        callback.nationAsyncResult(n);
    }
}
