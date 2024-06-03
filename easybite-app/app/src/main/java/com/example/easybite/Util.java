package com.example.easybite;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class Util {
    public static void downloadBitmapToImageView(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(url)
                .into(imageView);
    }
}
