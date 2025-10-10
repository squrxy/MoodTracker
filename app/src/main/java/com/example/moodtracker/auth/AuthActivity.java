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
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: подружить с твоим API
            api.login(new LoginRequest(email, pass)).enqueue(new SimpleAuthCallback());
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }
            // username временно = email до "@"
            String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            api.register(new RegisterRequest(username, email, pass)).enqueue(new SimpleAuthCallback());
        });

        btnGuest.setOnClickListener(v -> {
            // если серверного /auth/guest пока нет — создаём локальный guest
            // иначе: api.guest().enqueue(new SimpleAuthCallback());
            session.ensureLocalGuest();
            goMain();
        });
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /** единый колбэк для login/register/guest */
    private class SimpleAuthCallback implements Callback<AuthResponse> {
        @Override public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
            if (resp.isSuccessful() && resp.body() != null && resp.body().success) {
                AuthResponse r = resp.body();
                session.saveSession(r.token != null ? r.token : r.userId, r.userId, r.isGuest);
                goMain();
            } else {
                Toast.makeText(AuthActivity.this, "Auth failed", Toast.LENGTH_SHORT).show();
            }
        }
        @Override public void onFailure(Call<AuthResponse> call, Throwable t) {
            Toast.makeText(AuthActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
