package io.agora.agolet.data;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Lucy on 8/28/17.
 */
public class MessageBody extends SugarRecord {
    @SerializedName("cmid")
    String cmid;

    @SerializedName("messages")
    List<Message> messages;

    public String getMcid() {
        return cmid;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
