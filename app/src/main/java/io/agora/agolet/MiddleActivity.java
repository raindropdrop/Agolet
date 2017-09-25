package io.agora.agolet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import io.agora.agolet.data.Login;
import io.agora.agolet.data.User;

/**
 * Created by Lucy on 8/30/17.
 */
public class MiddleActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        // Check if skip Login Activity
        if (extras != null) {
            if (extras.getString("goMain").equals("goMain")) {
                // If directly go to Main Activity, pass info of user already logged in
                List<Login> loginList = Login.listAll(Login.class);
                Login loginTemp = loginList.get(0);
                Intent i = new Intent(MiddleActivity.this, MainActivity.class);
                User userTemp = User.find(User.class, "uid=?", loginTemp.getUid()).get(0);
                i.putExtra("login_name", userTemp.getEmail());
                i.putExtra("uid", loginTemp.getUid());
                i.putExtra("token", loginTemp.getUid());
                startActivity(i);
            }
        } else {
            Intent i = new Intent(MiddleActivity.this, LoginActivity.class);
            startActivity(i);
        }

    }
}
