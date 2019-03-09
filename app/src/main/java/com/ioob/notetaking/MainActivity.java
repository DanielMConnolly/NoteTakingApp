package com.ioob.notetaking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{
    Cursor todoCursor;
    ListView noteList;
    NoteAdapter adapter;
    SQLiteDatabase db;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noteList = (ListView) findViewById(R.id.note_list);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, R.layout.sort_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        loadNotesFromDatabase();
        noteList.setVisibility(View.GONE);

        noteList.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                    {
                        refreshList();
                        Toast.makeText(MainActivity.this, "NOTES REFRESHED", Toast.LENGTH_LONG).show();
                    }

                }
                return false;
            }
        });


    }
    public void loadNotesFromDatabase() {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes", null);

        // Create an instance of the NoteAdapter with our cursor
        adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openNote = new Intent(MainActivity.this, NoteActivity.class);
                openNote.putExtra("noteId", id);
                startActivity(openNote);
            }
        });
    }

    public void refreshList(){
        todoCursor = db.rawQuery("SELECT * FROM notes", null);
        adapter.changeCursor(todoCursor );
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database cursor
        if (todoCursor != null) {
            todoCursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note:
                // TODO something
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("NOTESTATUS","Notes being loaded");
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    loadNotesFromDatabase();
                }
                else {
                    // TODO tell the user we need permission for our app to work
                    Log.i("NOTESTATUS", "Notes not being loaded");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);

                }
                break;
        }
    }

    public boolean userHasPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
