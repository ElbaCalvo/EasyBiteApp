package com.example.easybite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText userEmail;
    private EditText userPassword;
    private Button accessButton;
    private TextView registerRedirect;
    private Context context = this;
    private RequestQueue requestQueue;
    private ProgressBar pb1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.passwd);
        accessButton = findViewById(R.id.access);
        registerRedirect = findViewById(R.id.register_redirect);
        pb1 = findViewById(R.id.loadingScreen);
        requestQueue = Volley.newRequestQueue(this);

        accessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();
                if (validateLogin(email)) {
                    pb1.setVisibility(View.VISIBLE);
                    loginUser(email, password);
                }
            }
        });
        registerRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateLogin(String email) {
        if (!email.contains("@") || email.length() < 8) {
            userEmail.setError("Formato inválido de email");
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Server.name + "/user/sessions",
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String receivedToken;
                        try {
                            receivedToken = response.getString("SessionToken");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.print("aaaaaaaa");
                        Toast.makeText(context, "Token: " + receivedToken, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);

                        SharedPreferences preferences = context.getSharedPreferences("AIRPEEK_APP_PREFS", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("VALID_EMAIL", email);
                        editor.putString("VALID_TOKEN", receivedToken);
                        editor.commit();
                        pb1.setVisibility(View.GONE); // Alternamos entre la visibilidad de la barra de progresión a nuestra conveniencia.
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            pb1.setVisibility(View.GONE);
                            Toast.makeText(context, "La conexión no se ha establecido", Toast.LENGTH_LONG).show();
                        } else {
                            pb1.setVisibility(View.GONE);
                            int serverCode = error.networkResponse.statusCode;
                            Toast.makeText(context, "Estado de respuesta " + serverCode, Toast.LENGTH_LONG).show();
                        }
                        error.printStackTrace();
                    }
                }
        );
        this.requestQueue.add(request);
    }
}
