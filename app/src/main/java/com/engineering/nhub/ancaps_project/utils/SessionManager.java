package com.engineering.nhub.ancaps_project.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.engineering.nhub.ancaps_project.Login;

import java.util.HashMap;

/**
 * Created by samuel on 2/23/18.
 */

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    Util util = new Util();

    private static final String PREFS_NAME = "LoginPrefs";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";




    //Constructor
    public SessionManager(Context context) {
        this._context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(String phone, String name, String lName, String tin, String email) {
        //storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        //storing phone in sharedPreference



        // commit changes
        editor.commit();
    }

    public void checkLogin() {
        // check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent intent = new Intent(_context, Login.class);
            // Closing all the Activities
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //starting Login activity
            _context.startActivity(intent);
        }
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // phone number

        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent intent = new Intent(_context, Login.class);
        // Closing all the Activities
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Starting Login activity
        _context.startActivity(intent);
    }

    // Quick check for login
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }

}
