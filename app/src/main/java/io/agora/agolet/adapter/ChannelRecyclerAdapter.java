package io.agora.agolet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.agora.agolet.MainActivity;
import io.agora.agolet.R;
import io.agora.agolet.data.Channel;

/**
 * Created by Lucy on 8/16/17.
 */
public class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerAdapter.ChannelViewHolder> {

    private List<Channel> channelList;
    private Context mContext;

    public static class ChannelViewHolder extends RecyclerView.ViewHolder{

        private TextView txtTitle;
        private View line;
        private TextView txtChannelName;
        private View end_padder;
        private LinearLayout container;
        private int viewType;

        public ChannelViewHolder(View itemView, int viewType) {
            super(itemView);
            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            line = itemView.findViewById(R.id.line);
            txtChannelName = (TextView) itemView.findViewById(R.id.txtChannelName);
            end_padder = itemView.findViewById(R.id.end_padder);
            container = (LinearLayout) itemView.findViewById(R.id.container);

            this.viewType = viewType;
        }
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View channelRow = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.channel_row,parent,false);
        ChannelViewHolder vh = new ChannelViewHolder(channelRow, viewType);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ChannelViewHolder holder, final int position) {

        // Give each channel a tag according to subscription status
        if (channelList.get(position).getSubscribed()){
            holder.txtTitle.setText("已 订 阅");
        } else {
            holder.txtTitle.setText("未 订 阅");
        }

        // Decide whether to show the tag or not according to the viewtype of channel
        if (holder.viewType == 0){
            holder.txtTitle.setVisibility(View.VISIBLE);
            holder.line.setVisibility(View.VISIBLE);
        } else if (holder.viewType == 1){
            holder.end_padder.setVisibility(View.VISIBLE);
        }

        // Set channel name and onclick listener
        holder.txtChannelName.setText(channelList.get(position).getName());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mContext instanceof MainActivity){
                    ((MainActivity)mContext).showChannelDetail(channelList.get(position).getSubscribed(), position);
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position){
        return channelList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    public ChannelRecyclerAdapter(Context context){
        this.mContext = context;
        channelList = new ArrayList<Channel>();
    }

    /**
     * Update and check if channel to be created already exist in database
     * @param channel
     * @return
     */
    public boolean updateChannel(Channel channel) {
        List<Channel> result = Channel.find(Channel.class, "name=?", channel.getName());
        if (result.size() == 0) {
            addChannel(channel);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add channel when refresh database
     * @param channel
     */
    public void addChannel(Channel channel){
        channelList.add(channel);
        notifyDataSetChanged();
    }

    /**
     * Clear database when refresh database
     */
    public void deleteAllChannels(){
        channelList.clear();
        notifyDataSetChanged();
    }

}
