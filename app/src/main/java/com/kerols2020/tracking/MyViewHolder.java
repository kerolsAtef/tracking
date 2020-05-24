package com.kerols2020.tracking;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder
  {

        ImageView imageView;
        TextView name;
        public MyViewHolder(@NonNull View itemView)
        {
        super(itemView);
            name=itemView.findViewById(R.id.friendName);
           imageView=itemView.findViewById(R.id.list_row_image);
        }
  }
