package com.him.note_him_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.him.note_him_android.adapter.NoteAdapter;
import com.him.note_him_android.adapter.SubjectAdapter;
import com.him.note_him_android.database.DatabaseHelper;
import com.him.note_him_android.database.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NoteActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    FloatingActionButton addNote;
    RecyclerView noteRecycler;
    NoteAdapter adapter;
    String subject;
    Button name, date;
    List<Note> noteList;

    @Override
    protected void onResume() {
        super.onResume();
        noteList = databaseHelper.getAllNotes(subject);
        adapter.updateList(noteList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchViewItem
                = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) searchViewItem.getActionView();
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        noteList = databaseHelper.searchNote(newText, subject);
                        adapter.updateList(noteList);
                        return false;
                    }
                });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        databaseHelper = new DatabaseHelper(this);
        noteRecycler = findViewById(R.id.noteRecycler);
        name = findViewById(R.id.name);
        date = findViewById(R.id.date);
        addNote = findViewById(R.id.addNote);
        subject = getIntent().getExtras().getString("subject");
        noteList = databaseHelper.getAllNotes(subject);
        adapter = new NoteAdapter(noteList, this);
        noteRecycler.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

//                int position_dragged = dragged.getAdapterPosition();
//                int position_dragged = viewHolder.getAdapterPosition();
//                int position_target = target.getAdapterPosition();
//
//                Collections.swap(recyclerJavaClassArrayList,position_dragged,position_target);
//                adapter.notifyItemMoved(position_dragged,position_target);


                return false;
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                databaseHelper.deleteNote(noteList.get(viewHolder.getAdapterPosition()).getId());

                noteList = databaseHelper.getAllNotes(subject);
                adapter.updateList(noteList);

            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView
                                                recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags(0, swipeFlags);

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                ColorDrawable swipeBackground = new ColorDrawable(Color.RED);
                Drawable deleteIcon = ContextCompat.getDrawable(NoteActivity.this, R.drawable.ic_delete);

                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

                if (dX > 0) {
                    swipeBackground.setBounds(itemView.getLeft(), itemView.getTop(), Math.round(dX), itemView.getBottom());
                    deleteIcon.setBounds(itemView.getLeft() + iconMargin - Math.round(iconMargin / 2), itemView.getTop() + iconMargin, itemView.getLeft() +
                            iconMargin + deleteIcon.getIntrinsicWidth(), itemView.getBottom() - iconMargin);

                } else {
                    swipeBackground.setBounds(itemView.getRight() + Math.round(dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteIcon.setBounds(itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(), itemView.getTop() + iconMargin,
                            itemView.getRight() - iconMargin + Math.round(iconMargin / 2), itemView.getBottom() - iconMargin);
                }

                swipeBackground.draw(c);
                deleteIcon.draw(c);
                c.save();

                if (dX > 0) {
                    c.clipRect(itemView.getLeft(), itemView.getTop(), Math.round(dX), itemView.getBottom());
                } else {
                    c.clipRect(itemView.getRight() + Math.round(dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                }
                c.restore();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        }).attachToRecyclerView(noteRecycler);


        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NoteActivity.this, AddNoteActivity.class).putExtra("subject", subject));
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(noteList, new Comparator<Note>() {
                    public int compare(Note obj1, Note obj2) {
                        // ## Ascending order
//                        return obj1.getNote().compareToIgnoreCase(obj2.getNote()); // To compare string values
                        try {
                            return new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").parse(obj1.getTimestamp()).compareTo(new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").parse(obj2.getTimestamp()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                        // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                        // ## Descending order
                        // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                        // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                    }
                });
                adapter.updateList(noteList);
            }
        });
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(noteList, new Comparator<Note>() {
                    public int compare(Note obj1, Note obj2) {
                        // ## Ascending order
                        return obj1.getNote().compareToIgnoreCase(obj2.getNote()); // To compare string values
                        // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                        // ## Descending order
                        // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                        // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                    }
                });
                adapter.updateList(noteList);
            }
        });
    }
}
