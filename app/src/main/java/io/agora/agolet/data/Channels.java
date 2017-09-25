package io.agora.agolet.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Lucy on 8/23/17.
 */
public class Channels {

    @SerializedName("channels")
    public List<Channel> channels;

    public List<Channel> getChannels(){
        return channels;
    }
}
