package io.agora.agolet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.agora.agolet.R;
import io.agora.agolet.data.Message;
import io.agora.agolet.data.User;

/**
 * Created by Lucy on 8/28/17.
 */
public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private Context mContext;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView txtSender;
        private TextView txtTime;
        private TextView txtContent;
        private ImageView imgRead;

        public MessageViewHolder(View itemView) {
            super(itemView);
            txtSender = (TextView) itemView.findViewById(R.id.txtSender);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
            txtContent = (TextView) itemView.findViewById(R.id.txtContent);
            imgRead = (ImageView) itemView.findViewById(R.id.imgRead);
        }
    }

    @Override
    public MessageRecyclerAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View messageRow = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.message_row,parent,false);
        MessageViewHolder vh = new MessageViewHolder(messageRow);

        return vh;
    }

    @Override
    public void onBindViewHolder(MessageRecyclerAdapter.MessageViewHolder holder, int position) {
        List<User> result = User.find(User.class,
                "uid=?",
                messageList.get(position).getUid().toString());

        holder.txtSender.setText(result.get(0).getNick());
        holder.txtTime.setText(getDate(messageList.get(position).getTs()));
        holder.txtContent.setText(messageList.get(position).getBody());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public MessageRecyclerAdapter(Context context){
        this.mContext = context;
        messageList = Message.listAll(Message.class);
    }

    public void addMessage(Message message){
        messageList.add(message);
        notifyDataSetChanged();
    }

    public void deleteAllMessages(){
        messageList.clear();
        notifyDataSetChanged();
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }
}
