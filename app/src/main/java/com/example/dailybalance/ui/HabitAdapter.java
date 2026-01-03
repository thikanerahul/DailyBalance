package com.example.dailybalance.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailybalance.R;

import java.util.List;

import com.example.dailybalance.data.local.entity.Habit;

public class HabitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HABIT = 0;
    private static final int TYPE_ADD = 1;

    private List<Habit> habits;
    private OnHabitClickListener listener;
    private OnAddClickListener addListener;

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);

        void onHabitLongClick(Habit habit);
    }

    public interface OnAddClickListener {
        void onAddClick();
    }

    public HabitAdapter(List<Habit> habits, OnHabitClickListener listener, OnAddClickListener addListener) {
        this.habits = habits;
        this.listener = listener;
        this.addListener = addListener;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == habits.size()) {
            return TYPE_ADD;
        }
        return TYPE_HABIT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_habit_add, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_habit_card, parent, false);
            return new HabitViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ADD) {
            ((AddViewHolder) holder).itemView.setOnClickListener(v -> {
                if (addListener != null)
                    addListener.onAddClick();
            });
        } else {
            Habit habit = habits.get(position);
            HabitViewHolder hHolder = (HabitViewHolder) holder;
            hHolder.bind(habit, listener);
        }
    }

    @Override
    public int getItemCount() {
        return habits.size() + 1;
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textStreak;
        // ImageView imgHabitIcon; // Removed as per the new structure

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textHabitName);
            textStreak = itemView.findViewById(R.id.textStreak); // Assuming R.id.textStreak is correct for the new
                                                                 // layout
            // imgHabitIcon = itemView.findViewById(R.id.imgHabitIcon); // Removed
        }

        public void bind(Habit habit, OnHabitClickListener listener) {
            textName.setText(habit.name);
            textStreak.setText(habit.streak + " Days");
            itemView.setOnClickListener(v -> listener.onHabitClick(habit));
            itemView.setOnLongClickListener(v -> {
                listener.onHabitLongClick(habit);
                return true;
            });
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
