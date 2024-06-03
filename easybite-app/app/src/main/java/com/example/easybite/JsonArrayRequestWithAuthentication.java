package com.example.easybite;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class JsonArrayRequestWithAuthentication extends JsonArrayRequest {
    private Context context;
    private String url;

    public JsonArrayRequestWithAuthentication(int method, String url, @Nullable JSONArray jsonRequest, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener, Context context) {
        super(method, Server.name + url, jsonRequest, listener, errorListener);
        this.context = context;
        this.url = Server.name + url;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        SharedPreferences preferences = context.getSharedPreferences("EASYBITE_APP_PREFS", Context.MODE_PRIVATE);
        String sessionToken = preferences.getString("VALID_TOKEN", null);
        if (sessionToken == null) {
            throw new AuthFailureError();
        }
        HashMap<String, String> myHeaders = new HashMap<>();
        myHeaders.put("SessionToken", sessionToken);
        return myHeaders;
    }
}