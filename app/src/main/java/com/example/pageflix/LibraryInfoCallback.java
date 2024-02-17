package com.example.pageflix;

import com.example.pageflix.entities.User;

public interface LibraryInfoCallback {
    void onLibraryInfoReceived(User library);
}