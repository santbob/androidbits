package com.santhoshn.androidbits.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by santhosh on 23/01/16.
 */
public class VolleyRequestHandler {
    private static VolleyRequestHandler mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    public final String TAG = getClass().getSimpleName();
    public static final String AUTH_AND_TRANSACTION_TAG = "AuthandTxn";
    private Set<String> supportedLocalesSet = new HashSet<String>();

    private VolleyRequestHandler(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        supportedLocalesSet.add("en_US");
        supportedLocalesSet.add("es_US");
        supportedLocalesSet.add("fr_CA");
        supportedLocalesSet.add("en_CA");

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(30);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static synchronized VolleyRequestHandler getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequestHandler(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        addToRequestQueue(req, TAG);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void cancelPendingRequests(Object tag) {
        getRequestQueue().cancelAll((tag != null) ? tag : TAG);
    }

    public void downloadImage(String imageUrl, ImageView intoImageView, int defaultImage, int defaultErrorImage) {
        if (TextUtils.isEmpty(imageUrl)) {
            intoImageView.setImageResource(defaultImage);
        } else {
            getImageLoader().get(getAbsoluteUrl(imageUrl), ImageLoader.getImageListener(intoImageView, defaultImage, defaultErrorImage));
        }
    }

    public void post(String url, Map<String, String> params, final ResponseHandler responseHandler, int maxNumRetries) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, getAbsoluteUrl(url), new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    VolleyLog.v("Response:%n %s", response.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(10000, maxNumRetries, 1.0f));
        addToRequestQueue(req);
    }

    public String getAbsoluteUrl(String url) {
        try {
            final URI u = new URI(url);
            if (u.isAbsolute()) {
                return url;
            } else {
                return "" + url; //empty url
            }
        } catch (URISyntaxException uriException) {
            return url;
        }
    }

    //get current set language code.
    private String getLanguageCode() {
        Locale locale = mCtx.getResources().getConfiguration().locale;
        String languageCode = locale.toString();
        return (supportedLocalesSet.contains(languageCode)) ? languageCode : "en_US";
    }
}
