package com.lloydtorres.stately;

import android.os.AsyncTask;
import android.util.Log;

import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Lloyd on 2016-01-10.
 */
public class NationAsyncTask extends AsyncTask<Void, Void, Nation> {
    private final String appTag = "com.lloydtorres.stately";

    private NationAsyncResponse callback;
    private String url = Nation.QUERY;

    public NationAsyncTask(NationAsyncResponse c, String nationName)
    {
        this.callback = c;
        this.url = String.format(this.url,nationName);
    }

    @Override
    protected Nation doInBackground(Void... params) {
        Nation response = null;

        try{
            URL javaURL = new URL(url);
            URLConnection conn = javaURL.openConnection();
            conn.setRequestProperty("User-Agent", appTag);
            conn.connect();
            Persister serializer = new Persister();
            response = serializer.read(Nation.class, conn.getInputStream());
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
