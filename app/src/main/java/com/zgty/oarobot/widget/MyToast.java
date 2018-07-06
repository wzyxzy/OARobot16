package com.zgty.oarobot.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zgty.oarobot.R;


/**
 * Created by YaoChen on 2017/4/18.
 */

public class MyToast {
    private Toast toast;

    public MyToast(Context context, String text, int success) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_mytoast, null);
        ImageView imageView = view.findViewById(R.id.toast_image);
        if (success == 0) {
            imageView.setBackgroundResource(R.mipmap.uc_success);
        } else {
            imageView.setBackgroundResource(R.mipmap.uc_prompt1);
        }

        TextView t = view.findViewById(R.id.toast_text);
        t.setText(text);
        if (toast != null) {
            toast.cancel();
        }
        toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
