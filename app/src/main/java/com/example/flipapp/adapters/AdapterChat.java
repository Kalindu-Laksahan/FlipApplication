package com.example.flipapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flipapp.models.ModelChat;
import com.example.foodorder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {


    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;

        FirebaseUser fUser;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        if (i==MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int i) {

    String message = chatList.get(i).getMessage();
    String timeStamp = chatList.get(i).getTimestamp();

    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    cal.setTimeInMillis(Long.parseLong(timeStamp));
    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

    holder.messageTv.setText(message);
    holder.timeTv.setText(dateTime);
    try {
        Picasso.get().load(imageUrl).into(holder.profileIv);
    }
    catch (Exception e) {

    }


    holder.messageLAyout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure to delete this message ?");

            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {

                    deleteMessage(i);

                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();

        }
    });



    if (i==chatList.size()-1) {
        if (chatList.get(i).isSeen()) {
            holder.isSeenTv.setText("Seen");
        }
        else {
            holder.isSeenTv.setText("Delivered");
        }
    }
    else {
        holder.isSeenTv.setVisibility(View.GONE);
    }

    }

    private void deleteMessage(int position) {

        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {

                    if (ds.child("sender").getValue().equals(myUID)) {

                        ds.getRef().removeValue();

                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        Toast.makeText(context, "You can delete only your messages", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class  MyHolder extends RecyclerView.ViewHolder {


        ImageView profileIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLAyout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTV);
            messageLAyout = itemView.findViewById(R.id.messageLayout);




        }
    }


}
