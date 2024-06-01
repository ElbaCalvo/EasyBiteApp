package com.example.easybite;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {
    private TextView usernameText;
    private TextView emailText;
    private TextView birthdateText;
    private RequestQueue requestQueue;
    private EditText email;
    private EditText username;
    private EditText password;
    private EditText password2;
    private EditText birthdate;
    private Button saveButton;
    private String sessionToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        birthdate = findViewById(R.id.birthdate);
        saveButton = findViewById(R.id.save_button);

        birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String month, day;
                                if (monthOfYear < 10) {
                                    month = "0" + (monthOfYear + 1);
                                } else {
                                    month = String.valueOf(monthOfYear + 1);
                                }
                                if (dayOfMonth < 10) {
                                    day = "0" + dayOfMonth;
                                } else {
                                    day = String.valueOf(dayOfMonth);
                                }
                                if (year < 2024) {
                                    birthdate.setText(year + "-" + month + "-" + day);
                                } else {
                                    birthdate.setText("");
                                }
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameNew = username.getText().toString();
                String passwordNew = password.getText().toString();
                String password2New = password2.getText().toString();
                String emailNew = email.getText().toString();
                String birthdateNew = birthdate.getText().toString();

                if (validateProfile(usernameNew, passwordNew, password2New, emailNew, birthdateNew)) {
                    updateProfile(usernameNew, passwordNew, emailNew, birthdateNew);
                }
            }
        });
    }

    private boolean validateProfile(String usernameNew, String passwordNew, String password2New, String emailNew, String birthdateNew) {
        if (usernameNew.toString().isEmpty() || passwordNew.toString().isEmpty() || password2.toString().isEmpty() || email.toString().isEmpty() || birthdate.toString().isEmpty()) {
            Toast.makeText(this, "Debes rellenar todos los campos!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!passwordNew.equals(password2New)) {
            password.setError("La contraseña no coincide");
            password2.setError("La contraseña no coincide");
            return false;
        }
        if (!emailNew.toString().contains("@") || emailNew.length() < 8) {
            email.setError("Formato inválido de email");
            return false;
        }
        return true;
    }

    private void updateProfile(String usernameNew, String passwordNew, String emailNew, String birthdateNew) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", usernameNew);
            requestBody.put("password", passwordNew);
            requestBody.put("email", emailNew);
            requestBody.put("birthdate", birthdateNew);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequestWithAuthentication request = new JsonObjectRequestWithAuthentication(
                Request.Method.PUT,
                "/user",
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseStatus = response.getString("response");
                            if ("not_ok".equals(responseStatus)) {
                                Toast.makeText(EditProfileActivity.this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                            } else if ("already_exist".equals(responseStatus)) {
                                Toast.makeText(EditProfileActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EditProfileActivity.this, "Error al parsear la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditProfileActivity.this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                    }
                }, this
        );
        requestQueue.add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
