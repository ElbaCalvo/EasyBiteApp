package com.example.easybite;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText password2EditText;
    private EditText emailEditText;
    private EditText birthdateEditText;
    private Button registerButton;
    private RequestQueue requestQueue;
    private final Context context = this;
    private ProgressBar pb1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        password2EditText = findViewById(R.id.password2);
        emailEditText = findViewById(R.id.email);
        birthdateEditText = findViewById(R.id.birthdate);
        pb1 = findViewById(R.id.loadingScreen);
        birthdateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String month, day;
                                if (monthOfYear < 10){
                                    month = "0" + (monthOfYear + 1);
                                } else {
                                    month = String.valueOf(monthOfYear + 1);
                                }
                                if (dayOfMonth < 10){
                                    day = "0" + dayOfMonth;
                                } else {
                                    day = String.valueOf(dayOfMonth);
                                }
                                if (year < 2024) {
                                    birthdateEditText.setText(year + "-" + month + "-" + day );
                                } else {
                                    birthdateEditText.setText("");
                                }
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        registerButton = findViewById(R.id.register_button);
        requestQueue = Volley.newRequestQueue(this);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String password2 = password2EditText.getText().toString();
                String email = emailEditText.getText().toString();
                String birthdate = birthdateEditText.getText().toString();
                if (validateRegister(username,password,password2, email, birthdate)){
                    pb1.setVisibility(View.VISIBLE);
                    sendRegisterRequest();
                }
            }
        });
    }
    private boolean validateRegister(String username, String password,String password2, String email, String birthdate){
        if (username.isEmpty() || password.isEmpty() || password2.isEmpty() || email.isEmpty()){
            Toast.makeText(this, "Debes rellenar todos los campos!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(password2)){
            passwordEditText.setError("Las contraseñas deben coincidir");
            password2EditText.setError("Las contraseñas deben coincidir");
            return false;
        }
        if (!email.contains("@") || email.length() < 8){
            emailEditText.setError("Formato inválido de email");
            return false;
        }
        if (birthdate.isEmpty()) {
            birthdateEditText.setError("La fecha de nacimiento no es válida ");
            return false;
        }
        return true;
    }

    private void sendRegisterRequest() {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", emailEditText.getText().toString());
            requestBody.put("username", usernameEditText.getText().toString());
            requestBody.put("password", passwordEditText.getText().toString());
            requestBody.put("birthdate", birthdateEditText.getText().toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Server.name + "/user",
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pb1.setVisibility(View.GONE);
                        Toast.makeText(context, "Usuario creado", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            pb1.setVisibility(View.GONE);
                            Toast.makeText(context, "No se pudo establecer la conexión", Toast.LENGTH_LONG).show();
                        } else {
                            pb1.setVisibility(View.GONE);
                            int serverCode = error.networkResponse.statusCode;
                            Toast.makeText(context, "Estado de respuesta: " + serverCode, Toast.LENGTH_LONG).show();
                        }

                    }
                }
        );
        this.requestQueue.add(request);
    }
}