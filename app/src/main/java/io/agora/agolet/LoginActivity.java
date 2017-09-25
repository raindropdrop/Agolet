package io.agora.agolet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.agora.agolet.data.Login;
import io.agora.agolet.data.User;
import io.agora.agolet.data.Users;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lucy on 8/15/17.
 */
public class LoginActivity extends AppCompatActivity implements Serializable{

    private String login_name;
    private String password;

    private EditText ed_login_name;
    private EditText ed_password;

    private boolean isLoginNameEmpty = true;
    private boolean isPassEmpty = true;

    private Gson gson;

    private Users users;
    private List<User> usersList;

    private static Login login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ed_login_name = (EditText) findViewById(R.id.login_name);
        ed_password = (EditText) findViewById(R.id.password);

        setupData();
        setupUI();

    }

    private void setupData() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    private void setupUI() {
        setupToolbar();
        setupTextbox();
        setupEditbox();
    }

    private void setupEditbox() {
        ed_login_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // Disable login button if either username or password is empty
                isLoginNameEmpty = TextUtils.isEmpty(s.toString());
                findViewById(R.id.btn_login).setEnabled(!(isLoginNameEmpty || isPassEmpty));

            }
        });


        ed_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // Disable login button if either username or password is empty
                isPassEmpty = TextUtils.isEmpty(s.toString());
                findViewById(R.id.btn_login).setEnabled(!(isLoginNameEmpty || isPassEmpty));
            }
        });
    }

    private void setupTextbox() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            login_name = extras.getString("login_name");
            ed_login_name.setText(login_name);
            isLoginNameEmpty = false;

        }
    }

    private void setupToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    /**
     * Login + Download all user info for display sender's name in message
     * @param view
     */
    public void onClickLogin(View view){

        login_name = ed_login_name.getText().toString();
        password = String.valueOf(ed_password.getText());


        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("account", login_name)
                .add("password", md5(password))
                .build();

        Request request = new Request.Builder()
                .url("http://agolet.agoralab.co/v1/login")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                response.close();

                if (!isValidJsonString(result)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();

                        }
                    });
                    return;
                }

                login = gson.fromJson(result, Login.class);
                if (Login.find(Login.class, "uid=?", login.getUid()).size() == 0){
                    login.save();
                }


                // Store all user info to local if not downloaded
                if (User.listAll(User.class).size() == 0){
                    getUsers(login);
                }

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("login_name", login_name);
                i.putExtra("uid", login.getUid());
                i.putExtra("token", login.getToken());
                i.putExtra("users", users);
                startActivity(i);


            }

        });
    }

    /**
     * MD5 hash conversion for password
     * @param s
     * @return
     */
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Download all user info
     * @param login
     */
    public void getUsers(Login login){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://agolet.agoralab.co/v1/userinfo")
                .get()
                .addHeader("uid", login.getUid())
                .addHeader("token", login.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "用户信息下载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                response.close();

                if (!isValidJsonString(result)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();

                        }
                    });
                    return;
                }

                    String data = result.replaceAll("\"id\"", "\"uid\"");

                    users = gson.fromJson(data, Users.class);
                    usersList = users.getUsers();

                    // Store all user data in database through sugar orm
                    for (User user : usersList){
                        user.save();
                    }


            }

        });
    }

    /**
     * Check if string downloaded is valid JSON string
     * @param json
     * @return
     */
    public static boolean isValidJsonString(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            try {
                new JSONArray(json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
