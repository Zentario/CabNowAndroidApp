package com.example.cabnow.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cabnow.Api.RetrofitClient;
import com.example.cabnow.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int LOCATION_PERMISSION_REQUEST = 1234;
    private boolean locationPermissionGranted = false;

    private Button btn_login;
    private EditText edt_phone, edt_password;
    private TextView tv_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean available = isPlayServiceAvailable();
        if(!available)
        {
            finishAffinity();
        }

        setupUI();

        getLocationPermission();


        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        Log.d(TAG, "onCreate: " + sharedPreferences.getString("phone_no", ""));
        edt_phone.setText(sharedPreferences.getString("phone_no", ""));
        edt_password.setText(sharedPreferences.getString("password", ""));


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mPhone = edt_phone.getText().toString();
                final String mPassword = edt_password.getText().toString();

                if(mPassword.equals("") || mPhone.equals(""))
                {
                    Toast.makeText(LoginActivity.this, "Fill out the fields", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    JSONObject params = new JSONObject();
                    try {
                        params.put("phone_no", mPhone);
                        params.put("password", mPassword);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    final AlertDialog dialog = new SpotsDialog.Builder()
                            .setContext(LoginActivity.this)
                            .setMessage("Logging in")
                            .setCancelable(false)
                            .build();

                    dialog.show();

                    Call<ResponseBody> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .loginUser(params.toString());

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            String s = null;

                            if(response.code() == 200 || response.code() == 202)
                            {
                                try {
                                    s = response.body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(s != null)
                            {
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    boolean error = jsonObject.getBoolean("error");
                                    String msg = jsonObject.getString("message");
                                    if(!error)
                                    {
                                        // Login success
                                        dialog.dismiss();
                                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();

                                        // TODO Start map activity from here
                                        addToSharedPref(mPhone, mPassword);
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        finish();
                                    }
                                    else
                                    {
                                        dialog.dismiss();
                                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            dialog.dismiss();
                            Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        if(!edt_phone.getText().toString().equals("") && !edt_password.getText().toString().equals(""))
        {
            btn_login.callOnClick();
        }

        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: checking permissions first");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(LoginActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "getLocationPermission: all permissions granted");
            locationPermissionGranted = true;
        }
        else
        {
            Log.d(TAG, "getLocationPermission: Requesting for permissions list");
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Looping through grantedResults");

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            locationPermissionGranted = true;

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Not granted");
                    locationPermissionGranted = false;
                }
            }

            if(locationPermissionGranted)
            {
                Log.d(TAG, "onRequestPermissionsResult: GRANTED");
            }
            else
            {
                Log.d(TAG, "onRequestPermissionsResult: Did not Grant");
                Toast.makeText(this, "This app needs location to function", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }
    }


    private boolean isPlayServiceAvailable() {
        Log.d(TAG, "isPlayServiceAvailable: checking play services availability");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);

        if(available == ConnectionResult.SUCCESS)
        {
            // maps can be used
            Log.d(TAG, "isPlayServiceAvailable: maps can be used");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            // user resolvable error
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            // cant use maps
            Toast.makeText(this, "You cannot make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void addToSharedPref(String phone, String pwd) {
        Log.d(TAG, "addToSharedPref: " + phone + " " + pwd);
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phone_no", phone);
        editor.putString("password", pwd);
        editor.apply();
    }

    private void setupUI() {
        btn_login = findViewById(R.id.btn_login);
        edt_phone = findViewById(R.id.edt_phone);
        edt_password = findViewById(R.id.edt_password);
        tv_signup = findViewById(R.id.tv_signup);
    }
}
