package com.example.dailybalance.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dailybalance.R;
import com.example.dailybalance.data.local.entity.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_glass, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        holder.title.setText(currentTask.title);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        holder.time.setText(sdf.format(new Date(currentTask.dateTime)));

        holder.completed.setChecked(currentTask.isCompleted);

        holder.completed.setOnClickListener(v -> {
            boolean isChecked = holder.completed.isChecked();
            if (listener != null) {
                currentTask.isCompleted = isChecked;
                listener.onTaskCheckChanged(currentTask);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTaskLongClick(currentTask);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onTaskCheckChanged(Task task);

        void onTaskLongClick(Task task);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        CheckBox completed;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTaskTitle);
            time = itemView.findViewById(R.id.textTaskTime);
            completed = itemView.findViewById(R.id.checkboxCompleted);
        }
    }
}
