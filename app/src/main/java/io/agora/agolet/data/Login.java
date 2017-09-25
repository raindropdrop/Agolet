package io.agora.agolet.data;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucy on 8/24/17.
 */
public class Login extends SugarRecord{

    @SerializedName("uid")
    String uid;

    @SerializedName("token")
    String token;

    public String getUid() {
        return uid;
    }

    public String getToken() {
        return token;
    }

}
