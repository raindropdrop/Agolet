package io.agora.agolet;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.agora.agolet.adapter.MessageRecyclerAdapter;
import io.agora.agolet.data.Channel;
import io.agora.agolet.data.Message;
import io.agora.agolet.data.MessageBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lucy on 8/17/17.
 */
public class DetailActivity extends AppCompatActivity {

    private String uid;
    private String token;

    private boolean subscribed = false;
    private String channelId;
    private Long channelMaxId;
    private Channel channel;

    private Gson gson;

    private List<Message> messages = new ArrayList<Message>();

    private RecyclerView recyclerView;
    private MessageRecyclerAdapter adapter;

    private SwipeRefreshLayout swipeRefreshView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            channel = (Channel) extras.get("channel");
            subscribed = channel.getSubscribed();
            channelId = channel.getCid();
            channelMaxId = channel.getMaxid();
            uid = extras.getString("uid");
            token = extras.getString("token");

        }


        setupData();
        setupUI();

    }

    private void setupData() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    private void setupUI() {
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();


        // Check if messagelist of the channel selected has already been loaded once
        if (MessageBody.listAll(MessageBody.class).size() == 0){
            getMessages(false);
        } else {
            if (MessageBody.find(MessageBody.class, "cmid=?",
                    channel.getCid().toString()).size() == 0){
                Log.d("asdfg", "getmessages");
                adapter.deleteAllMessages();
                getMessages(false);
            } else {
                Log.d("asdfg", "getmessageslocal");
                getMessagesLocal();
            }
        }
    }

    private void setupSwipeRefresh() {
        swipeRefreshView = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout_detail);
        swipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                regetMessages();
            }
        });
    }

    private void refreshItems() {
        regetMessages();
    }

    private void onItemsLoadComplete() {
        adapter.deleteAllMessages();
        setupMessages();
        swipeRefreshView.setRefreshing(false);
    }

    private void setupToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.drawable.ic_chevron_left_black_24dp);
        myToolbar.setTitle(channel.getName());

        View logo_view = myToolbar.getChildAt(1);
        logo_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_content);
        recyclerView.setHasFixedSize(true);

        final LinearLayoutManager mLayoutManagerUnsub =
                new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManagerUnsub);


        adapter = new MessageRecyclerAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupMessages() {

        for (Message m : messages) {
            m.save();
            adapter.addMessage(m);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        if (subscribed) {
            menu.findItem(R.id.action_subscribe).setTitle("取消订阅");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_subscribe:
                if (subscribed) {
                    item.setTitle("订阅");
                    setUnsubscribe();
                } else {
                    item.setTitle("取消订阅");
                    setSubsribe();
                }
                subscribed = !subscribed;

                break;
            default:
                break;
        }
        return true;
    }


    public void setSubsribe(){

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(null, new byte[]{});

        Request request = new Request.Builder()
                .url("http://agolet.agoralab.co/v1/subscribe/channel/" + String.valueOf(channelId))
                .post(body)
                .addHeader("uid", uid)
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("response", "fail");

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("response", response.body().string());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "订阅成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });

    }

    public void setUnsubscribe(){

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(null, new byte[]{});

        Request request = new Request.Builder()
                .url("http://agolet.agoralab.co/v1/unsubscribe/channel/" + String.valueOf(channelId))
                .post(body)
                .addHeader("uid", uid)
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("response", "fail");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "取消订阅成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });
    }

    private void getMessages(final boolean isReget){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://agolet.agoralab.co/v1/message/channel/" +
                        String.valueOf(channelId) +
                        "/maxId/" + String.valueOf(channelMaxId) +
                        "/minId/" + "0" +
                        "/limit/10")
                .get()
                .addHeader("uid", uid)
                .addHeader("token", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("response", "fail");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String data = response.body().string();
                response.close();

                if (!isValidJsonString(data)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show();

                        }
                    });
                    return;
                }

                // Avoid "id" field name conflict with SugarOrm
                data = data.replaceAll("\"id\"", "\"mid\"");
                data = data.replaceAll("\"channelId\"", "\"cmid\"");

                MessageBody messageBody = gson.fromJson(data, MessageBody.class);
                messageBody.save();
                messages = messageBody.getMessages();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isReget){
                            setupMessages();
                        }
                        else {
                            onItemsLoadComplete();

                        }
                    }
                });
            }


        });
    }

    private void getMessagesLocal(){
        Log.d("asdfg", "messagebodyid: " + channel.getName());
        for (Message m : (MessageBody.find(MessageBody.class,
                "cmid=?",
                channelId)).get(0).getMessages()){
            adapter.addMessage(m);
        }
    }

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

    private void regetMessages(){
        getMessages(true);
        refreshItems();
    }
}
