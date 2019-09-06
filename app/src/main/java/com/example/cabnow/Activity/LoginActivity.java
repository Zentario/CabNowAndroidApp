package com.example.cabnow.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cabnow.Api.RetrofitClient;
import com.example.cabnow.R;

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
    private Button btn_login;
    private EditText edt_phone, edt_password;
    private TextView tv_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupUI();

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
                                        //startActivity(LoginActivity.this, Home.class);
                                        //finish();
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

        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
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
