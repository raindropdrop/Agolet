package io.agora.agolet.data;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Lucy on 8/29/17.
 */
public class User extends SugarRecord{

    @SerializedName("nick")
    String nick;

    @SerializedName("email")
    String email;

    @SerializedName("uid")
    String uid;

    public String getNick() {
        return nick;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }


}
