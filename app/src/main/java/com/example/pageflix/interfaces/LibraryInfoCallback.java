package com.example.pageflix.interfaces;

import com.example.pageflix.entities.User;

public interface LibraryInfoCallback {
    void onLibraryInfoReceived(User library);
}