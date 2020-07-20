package com.him.note_him_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.him.note_him_android.AddNoteActivity;
import com.him.note_him_android.MainActivity;
import com.him.note_him_android.MapsActivity;
import com.him.note_him_android.R;
import com.him.note_him_android.ViewNote;
import com.him.note_him_android.database.Note;
import com.him.note_him_android.database.Subject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    List<Note> list;
    Context context;
    MediaPlayer mediaPlayer;

    public NoteAdapter(List<Note> list, Context context) {
        this.list = list;
        this.context = context;
    }
    public void updateList(List<Note> list){
        this.list = list;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_note, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteAdapter.ViewHolder holder, int position) {
        holder.name.setText(list.get(position).getNote());
        try {
            holder.subject.setText(new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").parse(list.get(position).getTimestamp()).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.address.setText(list.get(position).getAddress());
        holder.description.setText("Description :-"+list.get(position).getDescription());
        File file = new File(list.get(position).getImage());
        Uri uri = Uri.fromFile(file);
        holder.imageView.setImageURI(uri);
        final String audioLink = list.get(position).getAudio();
        if (!TextUtils.isEmpty(audioLink)) {
            holder.play.setVisibility(View.VISIBLE);
        } else {
            holder.play.setVisibility(View.GONE);
        }
        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mediaPlayer = new MediaPlayer();
//                try {
//                    mediaPlayer.setDataSource(audioLink);
//                    mediaPlayer.prepare();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                mediaPlayer.start();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, AddNoteActivity.class)
                        .putExtra("audio", list.get(holder.getAdapterPosition()).getAudio())
                        .putExtra("image", list.get(holder.getAdapterPosition()).getImage())
                        .putExtra("title", list.get(holder.getAdapterPosition()).getNote())
                        .putExtra("subject", list.get(holder.getAdapterPosition()).getSubject())
                        .putExtra("id", list.get(holder.getAdapterPosition()).getId())
                        .putExtra("desc", list.get(holder.getAdapterPosition()).getDescription())
                );
            }
        });
        holder.address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MapsActivity.class)
                        .putExtra("lat", list.get(holder.getAdapterPosition()).getLat())
                        .putExtra("lng", list.get(holder.getAdapterPosition()).getLng())
                        .putExtra("address", list.get(holder.getAdapterPosition()).getAddress())
                );
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MainActivity.class)
                        .putExtra("id", list.get(holder.getAdapterPosition()).getId())
                );
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, subject, address,description;
        ImageView imageView, play,edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            play = itemView.findViewById(R.id.play);
            subject = itemView.findViewById(R.id.subject);
            imageView = itemView.findViewById(R.id.image);
            address = itemView.findViewById(R.id.address);
            edit = itemView.findViewById(R.id.edit);
        }
    }

