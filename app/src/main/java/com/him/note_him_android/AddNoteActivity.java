package com.him.note_him_android;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.him.note_him_android.database.DatabaseHelper;
import com.him.note_him_android.database.Note;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddNoteActivity extends AppCompatActivity {

    ImageView imageView;
    private File destination = null;
    private File destinationAudio = null;
    private InputStream inputStreamImg;
    private String imgPath = null;
    Button addImage, save, record, play;
    private static final int RequestPermissionCode = 1052;
    private static final int RequestPermissionCodeLocation = 1050;
    private static final int RequestPermissionCodeCamera = 1051;
    DatabaseHelper databaseHelper;
    EditText title, description;
    String subject;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;
    Location locationGlobal;
    Boolean isRecording = false;

    // Location Demo with FUSED LOCATION PROVIDER CLIENT

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private static final int FASTEST_INTERVAL = 3000; // 3 seconds

    private List<String> permissionsToRequest;
    private List<String> permissions = new ArrayList<>();
    private List<String> permissionsRejected = new ArrayList<>();
    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;
    boolean isPlaying = false;
    int idSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        imageView = findViewById(R.id.imageView);
        play = findViewById(R.id.play);
        save = findViewById(R.id.save);
        record = findViewById(R.id.record);
        addImage = findViewById(R.id.addImage);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);

        databaseHelper = new DatabaseHelper(this);
        subject = getIntent().getExtras().getString("subject");
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            idSaved = bundle.getInt("id");
            String audioLink = bundle.getString("audio");
            if (!TextUtils.isEmpty(audioLink)) {
                destinationAudio = new File(audioLink);
                play.setVisibility(View.VISIBLE);
            }

            String imageLink = bundle.getString("image");
            if (!TextUtils.isEmpty(imageLink)) {
                destination = new File(imageLink);
                imageView.setImageURI(Uri.fromFile(destination));
            }
            title.setText(bundle.getString("title"));
            description.setText(bundle.getString("desc"));


        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionCamera()) {
                    selectImage(AddNoteActivity.this);
                } else {
                    requestPermissionCamera();
                }
            }
        });
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionRecord()) {
                    if (!isRecording) {
                        selectAudio(AddNoteActivity.this);
                    } else {
                        record.setText("Re-Record");
//                        v.setBackgroundResource(R.drawable.ic_record);
                        isRecording = false;
                        play.setVisibility(View.VISIBLE);
                        mediaRecorder.stop();
                    }
                } else {
                    requestPermission();
                }

            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isPlaying) {
                    mediaPlayer = new MediaPlayer();
                    isPlaying = true;
                    play.setText("Stop");
                    try {
                        mediaPlayer.setDataSource(destinationAudio.getAbsolutePath());
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            play.setText("Play");
                            isPlaying = false;
                        }
                    });
                } else {
                    play.setText("Play");
                    isPlaying = false;
                    mediaPlayer.stop();
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(AddNoteActivity.this, Locale.getDefault());

                String address = "";
                try {
                    if (checkPermissionLocation()) {
                        addresses = geocoder.getFromLocation(locationGlobal.getLatitude(), locationGlobal.getLongitude(), 1);
                        address = addresses.get(0).getAddressLine(0);
                    } else {
                        requestPermissionLocation();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(title.getText()) && TextUtils.isEmpty(description.getText())) {
                    Toast.makeText(AddNoteActivity.this, "Please enter Tittle and Description", Toast.LENGTH_SHORT).show();
                    return;
                }
                String imagePath = "", audioPath = "";
                if (destination != null) {
                    imagePath = destination.getAbsolutePath();
                }
                if (destinationAudio != null) {
                    audioPath = destinationAudio.getAbsolutePath();
                }
                Note note = new Note(title.getText().toString(), subject, address, "" + locationGlobal.getLatitude(), "" + locationGlobal.getLongitude(), description.getText().toString(), imagePath, audioPath);
                if (idSaved == 0) {
                    long id = databaseHelper.insertNote(note);
                    if (id > 0) {
                        Toast.makeText(AddNoteActivity.this, "Note Inserted Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    note.setId(idSaved);
                    long id = databaseHelper.updateNote(note);
                    if (id > 0) {
                        Toast.makeText(AddNoteActivity.this, "Note Update Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });
    }

    public void mediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        destinationAudio = new File(getFilesDir(), "AUD_" + timeStamp + ".3gp");
        mediaRecorder.setOutputFile(destinationAudio.getPath());
    }

    private void selectImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose option");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);//one can be replaced with any action code

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void selectAudio(Context context) {
        final CharSequence[] options = {"Record", "Choose from Files", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose option");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Record")) {
                    mediaRecorderReady();
                    isRecording = true;
                    record.setText("Stop");
//                    record.setBackgroundResource(R.drawable.ic_stop);
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else if (options[item].equals("Choose from Files")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*"); // specify "audio/mp3" to filter only mp3 files
                    startActivityForResult(intent, 26);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImage);
                        try {
                            selectedImage = (Bitmap) data.getExtras().get("data");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

                            Log.e("Activity", "Pick from Camera::>>> ");

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            destination = new File(getFilesDir(), "IMG_" + timeStamp + ".jpg");
                            FileOutputStream fo;
                            try {
                                destination.createNewFile();
                                fo = new FileOutputStream(destination);
                                fo.write(bytes.toByteArray());
                                fo.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            imgPath = destination.getAbsolutePath();
                            imageView.setImageBitmap(selectedImage);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        Bitmap bitmap;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                            Log.e("Activity", "Pick from Gallery::>>> ");

                            imgPath = getRealPathFromURI(selectedImage);
                            destination = new File(imgPath.toString());
                            imageView.setImageBitmap(bitmap);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case 26:
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            destinationAudio = new File(getPath(this, data.getData()));
                            play.setVisibility(View.VISIBLE);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }


                    }
                    break;

            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void requestPermissionCamera() {
        ActivityCompat.requestPermissions(AddNoteActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, CAMERA, READ_EXTERNAL_STORAGE}, RequestPermissionCodeCamera);


    private void requestPermissionLocation() {
        ActivityCompat.requestPermissions(AddNoteActivity.this, new
                String[]{ACCESS_FINE_LOCATION}, RequestPermissionCodeLocation);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(AddNoteActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCodeCamera:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(AddNoteActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                        selectImage(AddNoteActivity.this);
                    } else {
                        Toast.makeText(AddNoteActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RequestPermissionCodeLocation:
                boolean LocationPermission = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED;


                if (LocationPermission) {
                    Toast.makeText(AddNoteActivity.this, "Permission Granted",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddNoteActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                break;
            case RequestPermissionCode:
                boolean RecordPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (RecordPermission) {
                    Toast.makeText(AddNoteActivity.this, "Permission Granted",
                            Toast.LENGTH_LONG).show();
                    selectAudio(AddNoteActivity.this);
                } else {
                    Toast.makeText(AddNoteActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            if (mediaRecorder != null)
                mediaRecorder.stop();
            Log.d(TAG, "onStop: ");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // this is a proper place to check the google play services availability

        int errorCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, errorCode, errorCode, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Toast.makeText(AddNoteActivity.this, "No Services", Toast.LENGTH_SHORT).show();
                        }
                    });
            errorDialog.show();
        } else {
            Log.i(TAG, "onPostResume: ");
            findLocation();
        }
    }

    private void findLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        locationGlobal = location;
//                        location_tv.setText(String.format("Lat: %s, Lng: %s", location.getLatitude(), location.getLongitude()));
                    }
                }
            });
        }

        startUpdateLocation();
    }

    private void startUpdateLocation() {
        Log.d(TAG, "startUpdateLocation: ");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    locationGlobal = location;
//                    location_tv.setText(String.format("Lat: %s, Lng: %s", location.getLatitude(), location.getLongitude()));
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public boolean checkPermissionCamera() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);

        int result11 = ContextCompat.checkSelfPermission(getApplicationContext(),
                CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result11 == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissionRecord() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);

        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissionLocation() {

        int result11 = ContextCompat.checkSelfPermission(getApplicationContext(),
                ACCESS_FINE_LOCATION);
        return result11 == PackageManager.PERMISSION_GRANTED;
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
