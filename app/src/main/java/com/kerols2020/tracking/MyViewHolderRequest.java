package com.kerols2020.tracking;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolderRequest extends RecyclerView.ViewHolder
{

    TextView name,cancel,accept;
    public MyViewHolderRequest(@NonNull View itemView)
    {
        super(itemView);
        cancel=itemView.findViewById(R.id.cancel_request);
        accept=itemView.findViewById(R.id.accept_request);
        name=itemView.findViewById(R.id.sender_name);
    }
}