package com.santhoshn.androidbits.utils;

import org.json.JSONObject;

/**
 * Created by santhosh on 25/02/16.
 */
public interface ResponseHandler {
    void onSuccess(JSONObject response);

    void onError(JSONObject error);
}
