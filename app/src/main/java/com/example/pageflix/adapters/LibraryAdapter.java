package com.example.pageflix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pageflix.R;
import com.example.pageflix.entities.Library;

import java.util.List;

// Adapter for library RecyclerView
public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private final List<Library> libraries;
    private int selectedItem ;
    public LibraryAdapter(List<Library> libraries) {
        this.libraries = libraries;
        this.selectedItem = -1 ;
    }

    @NonNull
    @Override
    public LibraryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library, parent, false);
        return new LibraryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Library library = libraries.get(position);
        holder.libraryNameTextView.setText(library.getLibraryName());
        holder.libraryAddressView.setText("Address: " + library.getAddress());
        final int clickedPosition = position;

        // Set the selected state based on the position
        holder.itemView.setSelected(selectedItem == position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle selection state of the clicked item
                selectedItem = (selectedItem == clickedPosition) ? RecyclerView.NO_POSITION : clickedPosition;

                // Notify adapter about item selection change
                notifyDataSetChanged();
            }
        });
    }



    @Override
    public int getItemCount() {
        return libraries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView libraryNameTextView ;
        TextView libraryAddressView ;

        ViewHolder(View itemView) {
            super(itemView);
            libraryNameTextView = itemView.findViewById(R.id.nameTextView);
            libraryAddressView = itemView.findViewById(R.id.addressTextView);
        }
    }

    public int getSelectedItem(){
        return selectedItem ;
    }
}