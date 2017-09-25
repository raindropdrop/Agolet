package io.agora.agolet.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lucy on 8/29/17.
 */
public class Users implements Serializable{

    @SerializedName("users")
    List<User> users;

    public List<User> getUsers() {
        return users;
    }
}
