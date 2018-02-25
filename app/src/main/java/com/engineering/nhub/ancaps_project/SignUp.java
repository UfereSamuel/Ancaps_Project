package com.engineering.nhub.ancaps_project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.engineering.nhub.ancaps_project.utils.JsonParser;
import com.engineering.nhub.ancaps_project.utils.SessionManager;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONObject;

import java.util.HashMap;


public class SignUp extends AppCompatActivity {

    private EditText etPhone;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etPassword;
    private EditText etEmail;
    private EditText etCompanyName;

    private Button btnSignUp;
    private TextView tvLogin;
    private TextView tvTerms;

    private ConstraintLayout constLayout;
    private SessionManager sessionManager;
    private PopupWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etPhone = findViewById(R.id.etPhone);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etCompanyName = findViewById(R.id.etCompanyName);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this, Login.class));
                finish();
            }
        });

        constLayout = findViewById( R.id.consLayout);
        tvTerms = findViewById(R.id.tvTermsAndCondition);
        tvTerms.setMovementMethod(new ScrollingMovementMethod());

        /*tvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize a new instance of LayoutInflater service
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                // Inflate the custom layout/view
                View customView = inflater.inflate(R.layout.terms_conditions,null);
                // Initialize a new instance of popup window
                mPopupWindow = new PopupWindow(
                        customView,
                        Toolbar.LayoutParams.WRAP_CONTENT,
                        Toolbar.LayoutParams.WRAP_CONTENT
                );
                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                    mPopupWindow.setElevation(5.0f);
                }

                // Get a reference for the custom view close button
                ImageButton closeButton = customView.findViewById(R.id.ib_close);

                // Set a click listener for the popup window close button
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the popup window
                        mPopupWindow.dismiss();
                    }
                });
                // Finally, show the popup window at the center location of root relative layout
                mPopupWindow.showAtLocation(constLayout, Gravity.CENTER,0,0);


            }
        });*/

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone = etPhone.getText().toString().trim();
                String firstName = etFirstName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String companyName = etCompanyName.getText().toString().trim();

                etEmail.setError(null);

                if (phone.length() != 11) {
                    MDToast.makeText(getApplicationContext(), "Phone number cannot be " +
                            "less than 11", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }
                if (TextUtils.isEmpty(firstName)) {
                    MDToast.makeText(getApplicationContext(), "Firstname cannot be empty",
                            MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }
                if (TextUtils.isEmpty(lastName)) {
                    MDToast.makeText(getApplicationContext(), "Lastname cannot be empty",
                            MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }
                if (password.length() < 6) {
                    MDToast.makeText(getApplicationContext(), "Password cannot " +
                            "be less than 6", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }
                if (!EmailValidator.getInstance().validate(email)) {
                    etEmail.setError("invalid email");
                    MDToast.makeText(getApplicationContext(),
                            "Please provide a valid email", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();

                }
                if (TextUtils.isEmpty(companyName)) {
                    MDToast.makeText(getApplicationContext(), "Firstname cannot be empty",
                            MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }

                new sendPostRequest().execute();
            }
        });
    }

    public class sendPostRequest extends AsyncTask<String, Void, JSONObject>{

        JsonParser jsonParser = new JsonParser();
        private static final String SIGNUP_URL = "https://ancapps.herokuapp.com/register";

        String phone = etPhone.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String companyName = etCompanyName.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
            KProgressHUD.create(SignUp.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel("Please wait")
                    .setDetailsLabel("Registering..." + firstName)
                    .setCancellable(true)
                    .setBackgroundColor(Color.BLACK)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("phone", phone);
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("password", password);
                params.put("email", email);
                params.put("companyName", companyName);


                JSONObject json = jsonParser.makeHttpRequest(SIGNUP_URL, "POST", params);

                if (json != null) {

                    return json;
                } else {
                    Log.d("SignUp", json.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("SignUp", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            KProgressHUD.create(SignUp.this)
                    .dismiss();

            if (json != null) {

                SharedPreferences savedSession = getApplicationContext().getSharedPreferences(
                        "userinfo", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = savedSession.edit();
                editor.putString("phone", phone);
                editor.putString("firstName", firstName);
                editor.putString("lastName", lastName);
                editor.putString("password", password);
                editor.putString("email", email);
                editor.putString("companyName", companyName);


                editor.apply();

                MDToast.makeText(getApplicationContext(), "Registration successful",
                        MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                startActivity(new Intent(SignUp.this, MainActivity.class));
                finish();
            }
        }

    }

}
