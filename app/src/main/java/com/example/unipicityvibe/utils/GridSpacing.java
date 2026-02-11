package com.example.unipicityvibe.utils;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Κλάση για τη διαχείριση των αποστάσεων (spacing) μεταξύ των αντικειμένων σε ένα Grid layout
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position < 0) return;

        int column = position % spanCount; // υπολογισμός της στήλης που ανήκει το αντικείμενο

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) { // αν είναι στην πρώτη γραμμή, προσθέτουμε απόσταση από πάνω
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // απόσταση από κάτω
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing; // προσθήκη απόστασης πάνω για τα αντικείμενα εκτός της 1ης γραμμής
            }
        }
    }
}
