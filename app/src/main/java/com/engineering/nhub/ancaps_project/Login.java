package com.engineering.nhub.ancaps_project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.valdesekamdem.library.mdtoast.MDToast;

public class Login extends AppCompatActivity {

    private CheckBox rememberMe;
    private EditText uEmail;
    private EditText uPassword;
    private TextView uforgotPasword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        uEmail = (EditText) findViewById(R.id.etMail);
        uPassword = (EditText) findViewById(R.id.etPassword);
        rememberMe = (CheckBox) findViewById(R.id.checkbox_remMe);
        uforgotPasword = (TextView) findViewById(R.id.etforgot_password);

        uPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnLogin || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button btnLogin = findViewById(R.id.btnLogin);
        if (!new PrefManager(this).isUserLogedOut()) {
            //user's email and password both are saved in preferences
            startHomeActivity();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

    }

        private void startHomeActivity() {
            Intent login = new Intent(Login.this, HomePage.class);
            startActivity(login);

        }

        private void attemptLogin() {

            // Reset errors.
            uEmail.setError(null);
            uPassword.setError(null);

            // Store values at the time of the login attempt.
            final String email = uEmail.getText().toString().trim();
            final String password = uPassword.getText().toString().trim();

            if (!EmailValidator.getInstance().validate(email)) {
                uEmail.setError("invalid email");
                MDToast.makeText(getApplicationContext(),
                        "Please provide a valid email", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();

            } else if (TextUtils.isEmpty(password)) {
                uPassword.setError("empty password field");
                MDToast.makeText(getApplicationContext(), "Password " +
                        "field should not be empty", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
            } else if (password.length() < 5) {
                uPassword.setError("password too short");
            } else {

                if (rememberMe.isChecked())
                    saveLoginDetails(email, password);
                startHomeActivity();
            }
        }

    private void saveLoginDetails(String email, String password) {
        new PrefManager(this).saveLoginDetails(email, password);
    }

}
