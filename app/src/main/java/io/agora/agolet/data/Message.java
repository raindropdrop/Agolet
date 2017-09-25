package io.agora.agolet.data;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

/**
 * Created by Lucy on 8/28/17.
 */
public class Message extends SugarRecord{

    @SerializedName("ts")
    Long ts;

    @SerializedName("type")
    Long type;

    @SerializedName("body")
    String body;

    @SerializedName("uid")
    String uid;

    @SerializedName("cmid")
    String cmid;

    @SerializedName("mid")
    Long mid;

    Channel channel;

    public Long getTs() {
        return ts;
    }

    public Long getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public String getUid() {
        return uid;
    }

    public String getCmid() {
        return cmid;
    }

    public Long getMid() {
        return mid;
    }
}
