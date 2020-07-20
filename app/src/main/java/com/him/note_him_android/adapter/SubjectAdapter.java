package com.him.note_him_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.him.note_him_android.MainActivity;
import com.him.note_him_android.NoteActivity;
import com.him.note_him_android.R;
import com.him.note_him_android.database.DatabaseHelper;
import com.him.note_him_android.database.Subject;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {
    List<Subject> adapterList;
    Context context;
    boolean updateSubject;
    int id;
    DatabaseHelper databaseHelper;

    public SubjectAdapter(List<Subject> adapterList, Context context, boolean startActivity, int id, DatabaseHelper databaseHelper) {
        this.adapterList = adapterList;
        this.context = context;
        this.updateSubject = startActivity;
        this.id = id;
        this.databaseHelper = databaseHelper;
    }

    public void update(List<Subject> adapterList) {
        this.adapterList = adapterList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_subject, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.name.setText(adapterList.get(position).getSubject());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateSubject) {
                    databaseHelper.updateNoteSubject(id, adapterList.get(holder.getAdapterPosition()).getSubject());
                    ((MainActivity) context).finish();
                } else {
                    context.startActivity(new Intent(context, NoteActivity.class)
                            .putExtra("subject", adapterList.get(holder.getAdapterPosition()).getSubject()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }
}