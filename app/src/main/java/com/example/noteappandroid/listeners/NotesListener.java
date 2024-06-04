package com.example.noteappandroid.listeners;

import com.example.noteappandroid.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
