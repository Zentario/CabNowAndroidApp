package com.example.cabnow.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText edt_email, edt_name;
    private Button btn_signup;
    private EditText edt_phone, edt_password;
    private TextView tv_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mPhone = edt_phone.getText().toString();
                String mPassword = edt_password.getText().toString();
                String mName = edt_name.getText().toString();
                String mEmail = edt_email.getText().toString();

                if(mPhone.equals("") || mPassword.equals("") || mEmail.equals("") || mName.equals(""))
                {
                    Toast.makeText(MainActivity.this, "Fill out the fields", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    JSONObject paramObject = new JSONObject();
                    try {
                        paramObject.put("phone_no", mPhone);
                        paramObject.put("password", mPassword);
                        paramObject.put("email", mEmail);
                        paramObject.put("name", mName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    final AlertDialog dialog = new SpotsDialog.Builder()
                            .setContext(MainActivity.this)
                            .setMessage("Creating new user")
                            .setCancelable(false)
                            .build();

                    dialog.show();

                    Call<ResponseBody> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .createUser(paramObject.toString());

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.d(TAG, "onResponse: " + response.toString());
                            String s = null;
                            try {
                                if(response.code() == 201 || response.code() == 202)
                                {
                                    s = response.body().string();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if(s != null)
                            {
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    boolean error = jsonObject.getBoolean("error");
                                    String msg = jsonObject.getString("message");

                                    if(!error)
                                    {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                    else
                                    {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    private void setupUI() {
        edt_name = findViewById(R.id.edt_name);
        edt_email = findViewById(R.id.edt_email);
        btn_signup = findViewById(R.id.btn_signup);
        edt_phone = findViewById(R.id.edt_phone);
        edt_password = findViewById(R.id.edt_password);
        tv_login = findViewById(R.id.tv_login);
    }


}
