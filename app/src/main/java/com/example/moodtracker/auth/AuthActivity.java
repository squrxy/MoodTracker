package com.example.moodtracker.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moodtracker.MainActivity;
import com.example.moodtracker.R;
import com.example.moodtracker.net.ApiClient;
import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.net.dto.AuthResponse;
import com.example.moodtracker.net.dto.LoginRequest;
import com.example.moodtracker.net.dto.RegisterRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        session = new SessionManager(this);
        api = ApiClient.get().create(ApiService.class);

        TextInputLayout tilEmail = findViewById(R.id.tilEmail);
        TextInputLayout tilPassword = findViewById(R.id.tilPassword);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        MaterialButton btnGuest = findViewById(R.id.btnGuest);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            api.login(email, pass).enqueue(new SimpleAuthCallback());
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            api.register(username, email, pass).enqueue(new SimpleAuthCallback());
        });

        btnGuest.setOnClickListener(v -> api.guest().enqueue(new SimpleAuthCallback()));

    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /** единый колбэк для login/register/guest */
    private class SimpleAuthCallback implements retrofit2.Callback<AuthResponse> {
        @Override public void onResponse(retrofit2.Call<AuthResponse> call,
                                         retrofit2.Response<AuthResponse> resp) {
            if (resp.isSuccessful() && resp.body() != null && resp.body().success) {
                AuthResponse r = resp.body();
                session.saveSession(r.token != null ? r.token : r.userId, r.userId, r.isGuest);
                goMain();
            } else {
                String err = "";
                try { err = resp.errorBody() != null ? resp.errorBody().string() : ""; } catch (Exception ignored) {}
                android.widget.Toast.makeText(AuthActivity.this,
                        "Auth failed: " + (resp.body()!=null?resp.body().message:"") + " " + err,
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }
        @Override public void onFailure(retrofit2.Call<AuthResponse> call, Throwable t) {
            android.widget.Toast.makeText(AuthActivity.this,
                    "Network error: " + t.getMessage(),
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

}
