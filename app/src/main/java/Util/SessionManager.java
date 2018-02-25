package Util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.engineering.nhub.ancaps_project.MainActivity;

import java.util.HashMap;

/**
 * Created by iduma on 2/22/18.
 */

public class SessionManager {

    // Shared Preferences reference
    private final SharedPreferences pref;


    // Editor reference for Shared preferences
    private final SharedPreferences.Editor editor;

    //context
    private final Context _context;


    // Shared pref mode


    // Sharedpref file name
    private static  final String PREFER_NAME = "Ancaps";

    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "isUserLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_OBJECT_ID = "object_id";

    public static final String KEY_USER_TYPE = "userType";

    public static final String KEY_USER_ID = "phoneNumber";

    public static final String KEY_NAME = "name";







    // Constructor
    public SessionManager(Context context){
        final int PRIVATE_MODE = 0;
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.commit();
    }

    //Create login session

    public void createUserLoginSession(String object_id, String phoneNumber, String firstName,
                                       String lastName, String userType) {
        try {
            String name = firstName.concat(" ").concat(lastName);
            // Storing login value as TRUE
            editor.putBoolean(IS_USER_LOGIN, true);

            editor.putString(KEY_OBJECT_ID, object_id);

            editor.putString(KEY_USER_TYPE, userType);

            editor.putString(KEY_USER_ID, phoneNumber);

            editor.putString(KEY_NAME, name);



            editor.commit();
        } catch (Exception e) {
            //   Log.i("FAM", "ERR" + e.getMessage());
        }
    }
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     */

    public boolean checkLogin(){

        // Check login status
        if(!this.isUserLoggedIn()){

            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, MainActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

            return true;
        }
        return false;
    }



    public HashMap<String, String> getUserDetails() {

        HashMap<String, String> user = new HashMap<>();


        user.put(KEY_OBJECT_ID, pref.getString(KEY_OBJECT_ID, null));

        user.put(KEY_USER_TYPE, pref.getString(KEY_USER_TYPE, null));

        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));

        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        return user;
    }

    public void logoutUser() {

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, MainActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }


    // Check for login
    public boolean isUserLoggedIn() {
        return pref.getBoolean(IS_USER_LOGIN, false);
    }


}
