package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.database.HistoryEntity;
import com.example.myapplication.databinding.ActivityHistoryBinding;
import com.example.myapplication.viewmodel.HistoryViewModel;

/**
 * Geçmiş Projelerim Activity.
 * Room Database'den geçmiş kayıtlarını listeler.
 */
public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        observeHistory();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnHistoryActionListener(new HistoryAdapter.OnHistoryActionListener() {
            @Override
            public void onDelete(HistoryEntity entity) {
                viewModel.deleteHistory(entity);
                Toast.makeText(HistoryActivity.this,
                        getString(R.string.history_deleted), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(HistoryEntity entity) {
                Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
                intent.putExtra(HistoryDetailActivity.EXTRA_HISTORY_ID, entity.getId());
                intent.putExtra(HistoryDetailActivity.EXTRA_MODULE_TYPE, entity.getModuleType());
                intent.putExtra(HistoryDetailActivity.EXTRA_ORIGINAL_PATH, entity.getOriginalImagePath());
                intent.putExtra(HistoryDetailActivity.EXTRA_RESULT_PATH, entity.getResultImagePath());
                intent.putExtra(HistoryDetailActivity.EXTRA_TIMESTAMP, entity.getTimestamp());
                startActivity(intent);
            }
        });
    }

    private void observeHistory() {
        viewModel.getAllHistory().observe(this, historyList -> {
            if (historyList == null || historyList.isEmpty()) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
                adapter.setHistoryList(historyList);
            }
        });
    }
}

