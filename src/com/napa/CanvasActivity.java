package com.napa;

import com.napa.ScrollableViewGroup.OnCurrentViewChangedListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class CanvasActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   	    setContentView(R.layout.main);
        ViewGroup vg = (ViewGroup) findViewById(R.id.scroll);
        vg.setBackgroundColor(Color.WHITE);
        Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.icon0);
        ImageViewTouch c0 = new ImageViewTouch(this);
   	    c0.setImageBitmapReset(Bitmap.createScaledBitmap(bitmap0, 200, 200, true ), 0, true);
        vg.addView(c0);
        
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.icon1);
        ImageViewTouch c1 = new ImageViewTouch(this);
   	    c1.setImageBitmapReset(Bitmap.createScaledBitmap(bitmap1, 200, 200, true ), 0, true);
        vg.addView(c1);
        
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.icon2);
        ImageViewTouch c2 = new ImageViewTouch(this);
   	    c2.setImageBitmapReset(Bitmap.createScaledBitmap(bitmap2, 200, 200, true ), 0, true);
        vg.addView(c2);
        
        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.icon3);
        ImageViewTouch c3 = new ImageViewTouch(this);
   	    c3.setImageBitmapReset(Bitmap.createScaledBitmap(bitmap3, 200, 200, true ), 0, true);
        vg.addView(c3);
/*        ImageView c2 = new ImageView(this);
   	    c2.setImageResource(R.drawable.icon1);
        vg.addView(c2);
        
        ImageView c3 = new ImageView(this);
   	    c3.setImageResource(R.drawable.icon2);
        vg.addView(c3);
        
        ImageView c4 = new ImageView(this);
        c4.setImageResource(R.drawable.icon3);
        vg.addView(c4);
        
        ImageView c5 = new ImageView(this);
        c5.setImageResource(R.drawable.icon4);
        vg.addView(c5);
        
        ImageView c6 = new ImageView(this);
        c6.setImageResource(R.drawable.icon5);
        vg.addView(c6);
        */
    }

}