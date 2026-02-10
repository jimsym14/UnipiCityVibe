package com.example.unipicityvibe.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unipicityvibe.R;
import com.example.unipicityvibe.models.Event;
import com.example.unipicityvibe.ui.EventDetails;

import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = eventList.get(position);

        holder.tvTitle.setText(currentEvent.getTitle());

        if (currentEvent.getTicketPrice() == 0) {
            holder.tvPrice.setText("FREE");
            holder.tvPrice.setTextColor(context.getColor(R.color.price_color));
        } else {
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%.2f €", currentEvent.getTicketPrice()));
            holder.tvPrice.setTextColor(context.getColor(R.color.label_primary));
        }

        if (currentEvent.getTimestamp() > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d • HH:mm", Locale.ENGLISH);
            holder.tvDate.setText(sdf.format(new java.util.Date(currentEvent.getTimestamp())).toUpperCase());
        }

        if (currentEvent.getDistanceMeter() > 0) {
            holder.tvDistance.setText(currentEvent.getFormattedDistance());
            holder.tvDistance.setVisibility(View.VISIBLE);
            // πρασινο αν ειναι κοντα (<2km)
            if (currentEvent.getDistanceMeter() < 2000) {
                holder.tvDistance.setTextColor(context.getColor(R.color.price_color));
            } else {
                holder.tvDistance.setTextColor(context.getColor(R.color.label_secondary));
            }
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }

        String imageName = currentEvent.getImageResName();
        String imageUrl = currentEvent.getImageUrl();
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                .load(imageUrl)
                .override(600, 400) // downsample για γρηγοροτερο loading
                .centerCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .into(holder.imgEvent);
        } else if (imageName != null && !imageName.isEmpty()) {
            @SuppressLint("DiscouragedApi")
            int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.imgEvent.setImageResource(resId);
            } else {
                holder.imgEvent.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
             holder.imgEvent.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                EventDetails bottomSheet = EventDetails.newInstance(currentEvent);
                bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "EventDetailsBottomSheet");
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    
    // μεθοδος για αποδοτικη ενημερωση λιστας χωρις κολληματα
    public void updateEvents(List<Event> newEvents) {
        androidx.recyclerview.widget.DiffUtil.DiffResult diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return eventList.size();
            }

            @Override
            public int getNewListSize() {
                return newEvents.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                // ελεγχος βασει ID
                return eventList.get(oldItemPosition).getEventId().equals(newEvents.get(newItemPosition).getEventId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Event oldEvent = eventList.get(oldItemPosition);
                Event newEvent = newEvents.get(newItemPosition);
                // ελεγχος, ειδικα για την αποσταση που αλλαζει συχνα
                return oldEvent.getTitle().equals(newEvent.getTitle()) &&
                       oldEvent.getTimestamp() == newEvent.getTimestamp() &&
                       Math.abs(oldEvent.getDistanceMeter() - newEvent.getDistanceMeter()) < 1.0; 
            }
        });
        
        this.eventList = new java.util.ArrayList<>(newEvents);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvPrice, tvDistance;
        ImageView imgEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvPrice = itemView.findViewById(R.id.tvEventPrice);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            imgEvent = itemView.findViewById(R.id.imgEvent);
        }
    }
}