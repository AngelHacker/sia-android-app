package geek.jai.angelbot.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import geek.jai.angelbot.R;
import geek.jai.angelbot.modal.Chat;

/**
 * Created by JAID on 28-05-2016.
 */
public class ChatDisplayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> myChatList;
    private LayoutInflater myLayoutInflater;

    private static final int VIEW_CHAT_SENT = 1;
    private static final int VIEW_CHAT_RECEIVED = 2;
    private static final int VIEW_CHAT_PIC = 3;

    private Context myContext;

    public ChatDisplayAdapter(Context context, List<Chat> chatList) {
        myLayoutInflater = LayoutInflater.from(context);
        myChatList = chatList;
        myContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        if (viewType == VIEW_CHAT_SENT) {
            View view = myLayoutInflater.inflate(R.layout.chat_sent, parent, false);
            vh = new ChatSentViewHolder(view);
        } else if (viewType == VIEW_CHAT_RECEIVED) {
            View view = myLayoutInflater.inflate(R.layout.chat_received, parent, false);
            vh = new ChatReceivedViewHolder(view);
        } else if (viewType == VIEW_CHAT_PIC) {
            View view = myLayoutInflater.inflate(R.layout.image_card, parent, false);
            vh = new ChatPICViewHolder(view);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ChatSentViewHolder) {
            ChatSentViewHolder vh = (ChatSentViewHolder) holder;
            Chat chat = myChatList.get(position);
            vh.chatText.setText(chat.getChatText());

        } else if (holder instanceof ChatReceivedViewHolder) {
            ChatReceivedViewHolder vh = (ChatReceivedViewHolder) holder;
            Chat chat = myChatList.get(position);
            //Log.d(ChatDisplayAdapter.class.getSimpleName(), chat.getChatText());
            vh.chatText.setText(Html.fromHtml(chat.getChatText()));
        } else if (holder instanceof ChatPICViewHolder) {
            ChatPICViewHolder vh = (ChatPICViewHolder) holder;
            Chat chat = myChatList.get(position);
            /*
            Bitmap myBitmap = BitmapFactory.decodeFile(chat.getFileURI());
            vh.imageView.setImageBitmap(myBitmap);*/
            Picasso.with(myContext)
                    .load(new File(chat.getFileURI()))
                    .into(vh.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return myChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = myChatList.get(position);
        if (chat.getType() == 0) {
            return VIEW_CHAT_SENT;
        } else if (chat.getType() == 1) {
            return VIEW_CHAT_RECEIVED;
        } else if (chat.getType() == 2) {
            return VIEW_CHAT_PIC;
        } else {
            return super.getItemViewType(position);
        }

    }

    class ChatSentViewHolder extends RecyclerView.ViewHolder {
        private TextView chatText;

        public ChatSentViewHolder(View itemView) {
            super(itemView);
            chatText = (TextView) itemView.findViewById(R.id.chatSent);
        }
    }

    class ChatReceivedViewHolder extends RecyclerView.ViewHolder {
        private TextView chatText;

        public ChatReceivedViewHolder(View itemView) {
            super(itemView);
            chatText = (TextView) itemView.findViewById(R.id.chatReceived);
        }
    }

    class ChatPICViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ChatPICViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_pics);
        }
    }
}
