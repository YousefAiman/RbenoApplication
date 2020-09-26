package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerAdapter extends PagerAdapter {

    LayoutInflater layoutInflater;
    private Context context;
    private Integer[] images;
//    private String[] titles;

    //       = {R.drawable.facebook_icon,R.drawable.facebook_icon,R.drawable.facebook_icon,R.drawable.facebook_icon};
    ViewPagerAdapter(Context context, Integer[] images) {
        this.context = context;
        this.images = images;
//        this.titles = titles;

    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
//        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = layoutInflater.inflate(R.layout.pager_layout, null);
//        ImageView imageView = view.findViewById(R.id.pagerImageView);
//    TextView pagerTextLayout = view.findViewById(R.id.pagerTextLayout);
//        pagerTextLayout.setText(titles[position]);
//        if(position == 3)pagerTextLayout.setLines(2);

//        Drawable myDrawable =context.getResources().getDrawable(images[position]);
//        imageView.setImageDrawable(ContextCompat.getDrawable(context,images[position]));
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(images[position]);
        container.addView(imageView, 0);
//        ViewPager vp = (ViewPager) container;
//        vp.addView(view, 0);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
