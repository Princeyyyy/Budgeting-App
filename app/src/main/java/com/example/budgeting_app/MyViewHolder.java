package com.example.budgeting_app;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    View myView;
    public ImageView imageView;
    public TextView notes;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        myView = itemView;
        imageView = itemView.findViewById(R.id.imageview);
        notes = itemView.findViewById(R.id.notes);

    }

    public void setItemName(String itemName){
        TextView item = myView.findViewById(R.id.items);
        item.setText(itemName);
    }

    public void setItemAmount(String itemAmount){
        TextView amount = myView.findViewById(R.id.amounts);
        amount.setText(itemAmount);
    }

    public void setDate(String itemDate){
        TextView date = myView.findViewById(R.id.dates);
        date.setText(itemDate);
    }
}
