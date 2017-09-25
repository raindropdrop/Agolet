package io.agora.agolet.data;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucy on 8/24/17.
 */
public class Channel extends SugarRecord implements Serializable{
    @SerializedName("cid")
    String cid;

    @SerializedName("name")
    String name;

    @SerializedName("desc")
    String desc;

    @SerializedName("subscribed")
    boolean subscribed;

    @SerializedName("readmsgid")
    Long readmsgid;

    @SerializedName("maxid")
    Long maxid;

    private int type = 2;

    public String getCid(){
        return cid;
    }

    public String getName(){
        return name;
    }

    public String getDesc(){
        return desc;
    }

    public boolean getSubscribed() {
        return subscribed;
    }

    public Long getReadmsgid() {
        return readmsgid;
    }

    public Long getMaxid() {
        return maxid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

