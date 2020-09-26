package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class FilterFragment extends DialogFragment {

    TextView[] typeTextViews = new TextView[3];
    TextView[] categoryTextViews = new TextView[6];
    TextView[] priceTextViews = new TextView[2];
    TextView[] viewsTextViews = new TextView[2];
    TextView[] timeTextViews = new TextView[8];
    ArrayList<String> types;
    //    Spinner dateSpinner;
    int dateFilter;
    String category;
    int priceFilter = 0;
    int viewsFilter = 0;
    RatingBar filterRatingBar;

    public FilterFragment() {
        // Required empty public constructor
    }

    static FilterFragment newInstance() {
        return new FilterFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filter, container, false);

        view.findViewById(R.id.cancelIv).setOnClickListener(v -> dismiss());
        typeTextViews[0] = view.findViewById(R.id.typePromoTv_1);
        typeTextViews[1] = view.findViewById(R.id.typePromoTv_2);
        typeTextViews[2] = view.findViewById(R.id.typePromoTv_3);

//        for(int i=0;i<typeTextViews.length;i++){
//            typeTextViews[i] = view.findViewById(getResources().getIdentifier("R.id.typePromoTv_%"+i+1,
//                    "id", getContext().getPackageName()));
//        }


        timeTextViews[0] = view.findViewById(R.id.timePromoTv_1);
        timeTextViews[1] = view.findViewById(R.id.timePromoTv_2);
        timeTextViews[2] = view.findViewById(R.id.timePromoTv_3);
        timeTextViews[3] = view.findViewById(R.id.timePromoTv_4);
        timeTextViews[4] = view.findViewById(R.id.timePromoTv_5);
        timeTextViews[5] = view.findViewById(R.id.timePromoTv_6);
        timeTextViews[6] = view.findViewById(R.id.timePromoTv_7);
        timeTextViews[7] = view.findViewById(R.id.timePromoTv_8);

//        for(int i=1;i<=timeTextViews.length;i++){
//            timeTextViews[i-1] = view.findViewById(getResources().getIdentifier("R.id.timePromoTv_%"+i,
//                    "id", getContext().getPackageName()));
//        }

        categoryTextViews[0] = view.findViewById(R.id.homeScrollTv);
        categoryTextViews[1] = view.findViewById(R.id.mobileScrollTv);
        categoryTextViews[2] = view.findViewById(R.id.electronicScrollTv);
        categoryTextViews[3] = view.findViewById(R.id.furnitureScrollTv);
        categoryTextViews[4] = view.findViewById(R.id.carsScrollTv);
        categoryTextViews[5] = view.findViewById(R.id.pcScrollTv);

        priceTextViews[0] = view.findViewById(R.id.highPriceTv);
        priceTextViews[1] = view.findViewById(R.id.lowPriceTv);


        viewsTextViews[0] = view.findViewById(R.id.lowToHighViewsTv);
        viewsTextViews[1] = view.findViewById(R.id.highToLowViewsTv);
//        dateSpinner = view.findViewById(R.id.dateSpinner);

        filterRatingBar = view.findViewById(R.id.filterRatingBar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        typeClickListeners();
        categoryClickListeners();
        priceClickListeners();
        viewsClickListeners();
        timeClickListeners();


        view.findViewById(R.id.filterCheckImage).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            if (category != null && !category.isEmpty()) {
                bundle.putString("category", category);
            }

            bundle.putInt("price", priceFilter);

            bundle.putInt("views", viewsFilter);
            if (!types.isEmpty()) {
                bundle.putStringArrayList("promoType", types);
            }

            bundle.putInt("date", dateFilter);
            if (filterRatingBar.getRating() != 0) {
                bundle.putFloat("rating", filterRatingBar.getRating());
            }
            DialogFragment dialogFragment = FilteredPromosFragment.newInstance();
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getChildFragmentManager(), "filtered");
        });


    }

    void typeClickListeners() {
        types = new ArrayList<>();
        for (int i = 0; i < typeTextViews.length; i++) {
            final TextView textView = typeTextViews[i];
            int finalI = i + 1;
            textView.setOnClickListener(v -> {
                if (textView.getCurrentTextColor() == getResources().getColor(R.color.textGreyColor)) {
                    textView.setBackgroundResource(R.drawable.signinbuttonlayout);
                    textView.setTextColor(getResources().getColor(R.color.white));
                    switch (finalI) {
                        case 1:
                            addTypeToList("text");
                            break;
                        case 2:
                            addTypeToList("image");
                            break;
                        case 3:
                            addTypeToList("video");
                            break;
                    }
                } else {
                    textView.setBackgroundResource(R.drawable.signinedittext);
                    textView.setTextColor(getResources().getColor(R.color.textGreyColor));
                    switch (finalI) {
                        case 1:
                            types.remove("text");
                            break;
                        case 2:
                            types.remove("image");
                            break;
                        case 3:
                            types.remove("video");
                            break;
                    }
                }
            });
        }
    }

    void timeClickListeners() {

        for (int i = 0; i < timeTextViews.length; i++) {
            TextView textView = timeTextViews[i];
            int finalI = i;
            textView.setOnClickListener(v -> {
                if (textView.getCurrentTextColor() == getResources().getColor(R.color.textGreyColor)) {

                    for (TextView textView2 : timeTextViews) {
                        textView2.setBackgroundResource(R.drawable.signinedittext);
                        textView2.setTextColor(getResources().getColor(R.color.textGreyColor));
                    }
                    textView.setBackgroundResource(R.drawable.signinbuttonlayout);
                    textView.setTextColor(getResources().getColor(R.color.white));
                    dateFilter = finalI;

                } else {
                    textView.setBackgroundResource(R.drawable.signinedittext);
                    textView.setTextColor(getResources().getColor(R.color.textGreyColor));
                    dateFilter = 0;
                }
            });
        }
    }


    void categoryClickListeners() {
        for (TextView textView : categoryTextViews) {
            textView.setOnClickListener(v -> {
                if (textView.getCurrentTextColor() == getResources().getColor(R.color.textGreyColor)) {
                    for (TextView textView2 : categoryTextViews) {
                        textView2.setBackgroundResource(R.drawable.signinedittext);
                        textView2.setTextColor(getResources().getColor(R.color.textGreyColor));
                    }
                    textView.setBackgroundResource(R.drawable.signinbuttonlayout);
                    textView.setTextColor(getResources().getColor(R.color.white));
                    category = textView.getText().toString();
                } else {
                    textView.setBackgroundResource(R.drawable.signinedittext);
                    textView.setTextColor(getResources().getColor(R.color.textGreyColor));
                    category = "";
                }
            });
        }
    }

    void priceClickListeners() {
        for (int i = 0; i < priceTextViews.length; i++) {
            TextView tv = priceTextViews[i];
            int finalI = i;
            tv.setOnClickListener(v -> {
                if (tv.getCurrentTextColor() == getResources().getColor(R.color.textGreyColor)) {
                    for (TextView tv2 : priceTextViews) {
                        setTextViewBackground(tv2, R.drawable.signinedittext);
                        tv2.setTextColor(getResources().getColor(R.color.textGreyColor));
                    }
                    for (TextView tv2 : viewsTextViews) {
                        setTextViewBackground(tv2, R.drawable.signinedittext);
                        tv2.setTextColor(getResources().getColor(R.color.textGreyColor));
                    }
                    setTextViewBackground(tv, R.drawable.signinbuttonlayout);
                    tv.setTextColor(getResources().getColor(R.color.white));
                    viewsFilter = 0;
                    priceFilter = finalI + 1;
                } else {
                    setTextViewBackground(tv, R.drawable.signinedittext);
                    tv.setTextColor(getResources().getColor(R.color.textGreyColor));
                    priceFilter = 0;
                }
            });
        }
    }

    void viewsClickListeners() {
        for (int i = 0; i < viewsTextViews.length; i++) {
            TextView tv = viewsTextViews[i];
            int finalI = i;
            tv.setOnClickListener(v -> {
                if (tv.getCurrentTextColor() == getResources().getColor(R.color.textGreyColor)) {
                    for (TextView tv2 : viewsTextViews) {
                        setTextViewBackground(tv2, R.drawable.signinedittext);
                        tv2.setTextColor(getResources().getColor(R.color.textGreyColor));
                    }
                    for (TextView tv2 : priceTextViews) {
                        setTextViewBackground(tv2, R.drawable.signinedittext);
                        tv2.setTextColor(getResources().getColor(R.color.textGreyColor));
                    }
                    setTextViewBackground(tv, R.drawable.signinbuttonlayout);
                    tv.setTextColor(getResources().getColor(R.color.white));
                    priceFilter = 0;
                    viewsFilter = finalI + 1;
                } else {
                    setTextViewBackground(tv, R.drawable.signinedittext);
                    tv.setTextColor(getResources().getColor(R.color.textGreyColor));
                    viewsFilter = 0;
                }
            });
        }
    }

    void addTypeToList(String type) {
        if (!types.contains(type)) types.add(type);
    }

    void setTextViewBackground(TextView tv, int drawable) {
        tv.setBackgroundResource(drawable);
    }
}