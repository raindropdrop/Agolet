package io.agora.agolet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.agora.agolet.adapter.ChannelRecyclerAdapter;
import io.agora.agolet.data.Channel;
import io.agora.agolet.data.Channels;
import io.agora.agolet.data.Login;
import io.agora.agolet.data.Message;
import io.agora.agolet.data.MessageBody;
import io.agora.agolet.data.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    // These two lists are created for adapter usage
    private List<Channel> lsChSub = new ArrayList<Channel>();
    private List<Channel> lsChUnSub = new ArrayList<Channel>();

    // This list is created for sugar orm database usage
    public List<Channel> channelList = new ArrayList<Channel>();

    private String login_name;
    private String uid;
    private String token;

    private Gson gson;

    private RecyclerView recyclerView;
    private ChannelRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshView;

    private Channels channels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            login_name = extras.getString("login_name");
            uid = extras.getString("uid");
            token = extras.getString("token");
        }

        setupData();
        setupUI();

    }

    private void setupUI() {
        setupToolbar();
        setupRecyclerView();
        setupFloactingActionBtn();
        setupSwipeRefresh();

        /* Check if channel data has already been downloaded
         if so, get data from local */
        if (Channel.listAll(Channel.class).size() == 0) {
            getChannels(false);
        } else {
            getChannelsLocal();
        }

    }

    private void setupSwipeRefresh() {
        swipeRefreshView = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    private void refreshItems() {
        regetChannels();
    }

    private void onItemsLoadComplete() {
        adapter.deleteAllChannels();
        setupChannels();
        swipeRefreshView.setRefreshing(false);
    }

    private void setupToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        final LinearLayoutManager mLayoutManagerUnsub =
                new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManagerUnsub);


        adapter = new ChannelRecyclerAdapter(this);
        recyclerView.setAdapter(adapter);
    }


    private void getChannels(final boolean isReget){

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://agolet.agoralab.co/v1/channels")
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
                            Toast.makeText(MainActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show();

                        }
                    });
                    return;
                }

                // Avoid "id" field name conflict with SugarOrm
                data = data.replaceAll("\"id\"", "\"cid\"");

                channels = gson.fromJson(data, Channels.class);

                for (Channel c : channels.getChannels()){
                    // Save channel data here
                    c.save();
                    if (c.getSubscribed()) {
                        lsChSub.add(c);
                    } else {
                        lsChUnSub.add(c);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isReget){
                            setupChannels();
                        } else {
                            onItemsLoadComplete();
                        }
                    }
                });
            }


        });
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


    private void regetChannels(){
        lsChSub = new ArrayList<Channel>();
        lsChUnSub = new ArrayList<Channel>();
        getChannels(true);
    }

    /**
     * Set up channels in recyclerview
     */
    private void setupChannels(){

        for (Channel cs : lsChSub){
            if (lsChSub.indexOf(cs) == 0){
                cs.setType(0);
            } else if (lsChSub.indexOf(cs) == (lsChSub.size() - 1)) {
                cs.setType(1);
            }

            adapter.addChannel(cs);
        }

        for (Channel cu : lsChUnSub){
            if (lsChUnSub.indexOf(cu) == 0){
                cu.setType(0);
            }
            adapter.addChannel(cu);
        }

        channelList = Channel.listAll(Channel.class);
    }

    /**
     * Get channel data from local
     */
    private void getChannelsLocal(){
        for (Channel c : channelList){
            if (c.getSubscribed()){
                lsChSub.add(c);
            } else {
                lsChUnSub.add(c);
            }
        }
        setupChannels();
    }

    /**
     * Set add channel button
     */
    private void setupFloactingActionBtn() {

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View dialog_channel = inflater.inflate(R.layout.dialog_channel, null);
                final EditText etName = (EditText) dialog_channel.findViewById(R.id.etChannelName);
                final EditText etDesc = (EditText) dialog_channel.findViewById(R.id.etChannelDesc);
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("添加新频道")
                        .setView(dialog_channel)
                        .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                addChannel(etName.getText().toString(), etDesc.getText().toString());
                            }
                        })
                        .setNegativeButton("取消", null).create();


                dialog.show();
            }
        });
    }

    /**
     * Request to add new channel
     * @param name
     * @param desc
     */
    private void addChannel(String name, String desc){
        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("name", name)
                .add("desc", desc)
                .build();

        Request request = new Request.Builder()
                .url("http://agolet.agoralab.co/v1/newchannel")
                .post(body)
                .addHeader("uid", uid)
                .addHeader("token", token)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("response", "fail");

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Channel channel = gson.fromJson(response.body().string(), Channel.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = adapter.updateChannel(channel);
                        if (success) {
                            Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "频道已存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }


        });
    }

    private void setupData() {

        channelList = Channel.listAll(Channel.class);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View dialog_setting = inflater.inflate(R.layout.dialog_setting, null);
                final TextView etName = (TextView) dialog_setting.findViewById(R.id.txtLoginName);
                etName.setText(login_name);
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("用户信息")
                        .setView(dialog_setting)
                        .setPositiveButton("退出登录", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                i.putExtra("login_name", login_name);
                                startActivity(i);
                                finish();
                            }
                        })
                        .setNegativeButton("确定", null).create();
                dialog.show();

                break;
            default:
                break;
        }
        return true;
    }

    public void showChannelDetail(boolean subscribed, int position) {
        Intent i = new Intent(MainActivity.this, DetailActivity.class);

        if (subscribed) {
            i.putExtra("channel", lsChSub.get(position));
        } else {
            i.putExtra("channel", lsChUnSub.get(position));
        }
        i.putExtra("uid", uid);
        i.putExtra("token", token);
        startActivity(i);
    }


    /**
     * Clear all data except channel when main activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Channel.deleteAll(Channel.class);
        Login.deleteAll(Login.class);
        Message.deleteAll(Message.class);
        MessageBody.deleteAll(MessageBody.class);
        User.deleteAll(Message.class);
    }
}
