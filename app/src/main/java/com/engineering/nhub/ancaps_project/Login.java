package com.engineering.nhub.ancaps_project;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.engineering.nhub.ancaps_project.utils.JsonParser;
import com.engineering.nhub.ancaps_project.utils.SessionManager;
import com.engineering.nhub.ancaps_project.utils.Util;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private CheckBox rememberMe;
    private EditText uEmail;
    private EditText uPassword;
    private TextView uforgotPasword;
    Util util = new Util();
    SessionManager session;
    KProgressHUD hud;

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

        hud = KProgressHUD.create(Login.this);

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

        uforgotPasword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resetPassword = new Intent(getApplicationContext(), ResetPassword.class);
                startActivity(resetPassword);
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
                try {
                    attemptLogin();
                    startHomeActivity();
                } catch (Exception e){
                    Log.d("Posting to backend",e.getMessage());
                }
            }
        });

    }

        private void startHomeActivity() {
            Intent login = new Intent(Login.this, HomePage.class);
            startActivity(login);
            finish();

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
                if (util.isNetworkAvailable(getApplicationContext())) {
                    if (rememberMe.isChecked()) {
                        try {
                            saveLoginDetails(email, password);
                        } catch (Exception e){
                            Log.d("Saving details",e.getMessage());
                        }

                        new PostAsync().execute(email,password);
                    } else {
                        new PostAsync().execute(email,password);
                    }
                }
            }

        }

    private void saveLoginDetails(String email, String password) {
        new PrefManager(this).saveLoginDetails(email, password);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // 1
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); // 2
        return networkInfo != null && networkInfo.isConnected(); // 3
    }

  class PostAsync extends AsyncTask<String, String, JSONObject> {
        JsonParser jsonParser = new JsonParser();

        private static final String LOGIN_URL = "http://ancapps.herokuapp.com/login";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";


        @Override
        protected void onPreExecute() {
                hud.setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
                .setLabel("Please wait")
                .setMaxProgress(100)
                .show();
            hud.setProgress(90);
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> user = new HashMap<>();
                user.put("email", args[0]);
                user.put("password", args[1]);

                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", user);

                if (json != null) {
                    Log.d("JSON result", json.toString());

                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONObject json) {

            int success = 0;
            String message = "";

            if (json != null) {
                Toast.makeText(Login.this, json.toString(),
                        Toast.LENGTH_LONG).show();

                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Success!", message);
            }else{
                Log.d("Failure", message);
            }
        }

    }

}
