package com.example.unipicityvibe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unipicityvibe.R;
import com.example.unipicityvibe.models.BookingItem;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private List<BookingItem> data;
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(String id);
    }
    
    public BookingAdapter(List<BookingItem> data, OnDeleteListener deleteListener) {
        this.data = data;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         // Δημιουργία του View για κάθε γραμμή της λίστας
         View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
         return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Σύνδεση δεδομένων με το View
        BookingItem item = data.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvCode.setText(item.code);
        
        // Μορφοποίηση ημερομηνίας σε αναγνώσιμη μορφή
        if (item.timestamp > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            holder.tvDate.setText(holder.itemView.getContext().getString(R.string.date_prefix) + sdf.format(new java.util.Date(item.timestamp)));
        } else {
            holder.tvDate.setText("");
        }

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(item.id));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCode;
        ImageButton btnDelete;
        
        ViewHolder(View v) { 
            super(v); 
            tvTitle = v.findViewById(R.id.tvBookingTitle);
            tvDate = v.findViewById(R.id.tvBookingDate);
            tvCode = v.findViewById(R.id.tvBookingCode);
            btnDelete = v.findViewById(R.id.btnDeleteBooking);
        }
    }
}
