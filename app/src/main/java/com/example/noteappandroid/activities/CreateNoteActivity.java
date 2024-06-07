package com.example.noteappandroid.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noteappandroid.R;
import com.example.noteappandroid.database.NotesDatabase;
import com.example.noteappandroid.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime, textWebUrl;
    private LinearLayout layoutWebUrl;
    private ImageView imageNote;
    private BottomSheetDialog bottomSheetDialog;
    private View viewSubtitleIndicator;
    private String readPermissionImages, selectedNoteColor, selectedImagePath;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private AlertDialog dialogAddUrl, dialogDeleteNote;
    private Note alreadyAvaliableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initNotes();
        initMiscellaneous();
        setSubtitleIndicator();
    }

    private void initNotes() {
        ImageView imageBack = findViewById(R.id.imgBack);
        imageBack.setOnClickListener(v -> onBackPressed());

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNoteText);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        ImageView imgSave = findViewById(R.id.imgSave);
        imageNote = findViewById(R.id.imgNote);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebUrl = findViewById(R.id.layoutWebUrl);

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.ENGLISH).format(new Date())
        );

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvaliableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.imgRemoveWebUrl).setOnClickListener(v -> {
            layoutWebUrl.setVisibility(View.GONE);
            textWebUrl.setText(null);
        });

        findViewById(R.id.imgRemoveImage).setOnClickListener(v -> {
            imageNote.setImageBitmap(null);
            imageNote.setVisibility(View.GONE);
            findViewById(R.id.imgRemoveImage).setVisibility(View.GONE);
            selectedImagePath = "";
        });

        imgSave.setOnClickListener(v -> saveNote());
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvaliableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvaliableNote.getSubtitle());
        inputNoteText.setText(alreadyAvaliableNote.getNoteText());
        textDateTime.setText(alreadyAvaliableNote.getDateTime());

        if (alreadyAvaliableNote.getImagePath() != null && !alreadyAvaliableNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvaliableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imgRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvaliableNote.getImagePath();
        }

        if (alreadyAvaliableNote.getWebLink() != null && !alreadyAvaliableNote.getWebLink().trim().isEmpty()) {
            layoutWebUrl.setVisibility(View.VISIBLE);
            textWebUrl.setText(alreadyAvaliableNote.getWebLink());
        }

    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteSubtitle.getText().toString().trim().isEmpty() &&
                inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (layoutWebUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(textWebUrl.getText().toString());
        }

        if (alreadyAvaliableNote != null) {
            note.setId(alreadyAvaliableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        ImageView imgSetColor = findViewById(R.id.imgColor);
        bottomSheetDialog = new BottomSheetDialog(CreateNoteActivity.this);
        @SuppressLint("InflateParams") View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_miscellaneous, null);

        bottomSheetDialog.setContentView(bottomSheetView);
        imgSetColor.setOnClickListener(v -> {
            bottomSheetDialog.show();
        });

        List<ImageView> imgColorList = Arrays.asList(
                bottomSheetView.findViewById(R.id.imageColor1),
                bottomSheetView.findViewById(R.id.imageColor2),
                bottomSheetView.findViewById(R.id.imageColor3),
                bottomSheetView.findViewById(R.id.imageColor4),
                bottomSheetView.findViewById(R.id.imageColor5)
        );

        String[] colors = {"#333333", "#FDBE3B", "#FF4842", "#3A52Fc", "#000000"};
        for (int i = 0; i < imgColorList.size(); i++) {
            int index = i;
            imgColorList.get(i).setOnClickListener(v -> {
                selectedNoteColor = colors[index];
                selectedNoteColor(imgColorList, imgColorList.get(index));
                setSubtitleIndicator();
            });
        }

        if (alreadyAvaliableNote != null && alreadyAvaliableNote.getColor() != null && !alreadyAvaliableNote.getColor().trim().isEmpty()) {
            switch (alreadyAvaliableNote.getColor()) {
                case "#FDBE3B":
                    bottomSheetView.findViewById(R.id.imageColor2).performClick();
                    break;
                case "#FF4842":
                    bottomSheetView.findViewById(R.id.imageColor3).performClick();
                    break;
                case "#3A52Fc":
                    bottomSheetView.findViewById(R.id.imageColor4).performClick();
                    break;
                case "#000000":
                    bottomSheetView.findViewById(R.id.imageColor5).performClick();
                    break;
                case "#333333":
                    bottomSheetView.findViewById(R.id.imageColor1).performClick();
                    break;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readPermissionImages = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            readPermissionImages = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        bottomSheetView.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), readPermissionImages
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{readPermissionImages},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });

        bottomSheetView.findViewById(R.id.layoutAddUrl).setOnClickListener(v -> {
            showAddUrlDialog();
            bottomSheetDialog.dismiss();
        });

        if (alreadyAvaliableNote != null) {
            bottomSheetView.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.layoutDeleteNote).setOnClickListener(v -> {
                showDeleteNoteDialog();
                bottomSheetDialog.dismiss();
            });
        }
    }

    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note,
                    findViewById(R.id.layoutDeleteNoteContainer));
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(v -> {
                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvaliableNote);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Intent intent = new Intent();
                        intent.putExtra("isNoteDeleted", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
                new DeleteNoteTask().execute();
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDeleteNote.dismiss());
        }
        dialogDeleteNote.show();
    }

        private void setSubtitleIndicator () {
            GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
            gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
        }

        private void selectedNoteColor (List < ImageView > imgColorList, ImageView selectedImageView)
        {
            for (ImageView imageView : imgColorList) {
                imageView.setImageResource(0);
            }
            selectedImageView.setImageResource(R.drawable.ic_done);
        }

        // Add SuppressLint
        @SuppressLint("QueryPermissionsNeeded")
        private void selectImage () {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            }
        }

        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE_STORAGE_PERMISSION
                    && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imgRemoveImage).setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        private String getPathFromUri (Uri uri){
            String filePath;
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                filePath = uri.getPath();
            } else {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex("_data");
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
            return filePath;
        }

        private void showAddUrlDialog () {
            if (dialogAddUrl == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View view = getLayoutInflater().inflate(R.layout.layout_add_url, findViewById(R.id.layoutAddUrlContainer));
                builder.setView(view);
                dialogAddUrl = builder.create();
                if (dialogAddUrl.getWindow() != null) {
                    dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                final EditText inputUrl = view.findViewById(R.id.inputUrl);
                inputUrl.requestFocus();

                view.findViewById(R.id.textAdd).setOnClickListener(v -> {
                    if (inputUrl.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                        Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        textWebUrl.setText(inputUrl.getText().toString());
                        dialogAddUrl.dismiss();
                    }
                });
                view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogAddUrl.dismiss());
            }
            dialogAddUrl.show();
        }

    }