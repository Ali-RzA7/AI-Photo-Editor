package com.example.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.myapplication.R;
import com.example.myapplication.database.HistoryEntity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter: Geçmiş kayıtlarını listeler.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryEntity> historyList = new ArrayList<>();
    private OnHistoryActionListener listener;

    public interface OnHistoryActionListener {
        void onDelete(HistoryEntity entity);
        void onItemClick(HistoryEntity entity);
    }

    public void setOnHistoryActionListener(OnHistoryActionListener listener) {
        this.listener = listener;
    }

    public void setHistoryList(List<HistoryEntity> list) {
        this.historyList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntity entity = historyList.get(position);

        // Modül tipi
        holder.txtModuleType.setText(entity.getModuleType());

        // Tarih formatlama
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("tr"));
        String dateStr = sdf.format(new Date(entity.getTimestamp()));
        holder.txtDate.setText(dateStr);

        // Sonuç görseli (Glide ile yuvarlak köşeli)
        File resultFile = new File(entity.getResultImagePath());
        if (resultFile.exists()) {
            Glide.with(holder.itemView.getContext())
                    .load(resultFile)
                    .transform(new CenterCrop(), new RoundedCorners(24))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Silme
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(entity);
        });

        // Tıklama
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(entity);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView txtModuleType;
        TextView txtDate;
        ImageButton btnDelete;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            txtModuleType = itemView.findViewById(R.id.txt_module_type);
            txtDate = itemView.findViewById(R.id.txt_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
