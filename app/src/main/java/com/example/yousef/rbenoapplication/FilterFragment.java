package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class FilterFragment extends Fragment {

    private final TextView[]
            typeTextViews = new TextView[3],
            priceTextViews = new TextView[2],
            viewsTextViews = new TextView[2],
            timeTextViews = new TextView[8];
    private final ArrayList<String> types = new ArrayList<>(), categories = new ArrayList<>();
    private TextView[] categoryTextViews;
    private ImageView[] categoryImageViews;
    private int dateFilter, priceFilter = 0, viewsFilter = 0, textGreyColor, textWhiteColor;
    private RatingBar filterRatingBar;
    private String type;
    private Button filterButton;
    Fragment dialogFragment;

    public FilterFragment() {
        // Required empty public constructor
    }

    static FilterFragment newInstance() {
        return new FilterFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
//    ViewGroup viewGroup = (ViewGroup) view;
        ((Toolbar) view.findViewById(R.id.filterToolBar)).setNavigationOnClickListener(view1 ->
                getActivity().onBackPressed());

        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });


        textGreyColor = getResources().getColor(R.color.textGreyColor);
        textWhiteColor = getResources().getColor(R.color.white);
        view.findViewById(R.id.deleteFiltersTv).setOnClickListener(v -> clearAllFilters());


        typeTextViews[0] = view.findViewById(R.id.typePromoTv_1);
        typeTextViews[1] = view.findViewById(R.id.typePromoTv_2);
        typeTextViews[2] = view.findViewById(R.id.typePromoTv_3);


        timeTextViews[0] = view.findViewById(R.id.timePromoTv_1);
        timeTextViews[1] = view.findViewById(R.id.timePromoTv_2);
        timeTextViews[2] = view.findViewById(R.id.timePromoTv_3);
        timeTextViews[3] = view.findViewById(R.id.timePromoTv_4);
        timeTextViews[4] = view.findViewById(R.id.timePromoTv_5);
        timeTextViews[5] = view.findViewById(R.id.timePromoTv_6);
        timeTextViews[6] = view.findViewById(R.id.timePromoTv_7);
        timeTextViews[7] = view.findViewById(R.id.timePromoTv_8);


        categoryTextViews = new TextView[]{
                view.findViewById(R.id.homeScrollTv),
                view.findViewById(R.id.mobileScrollTv),
                view.findViewById(R.id.electronicScrollTv),
                view.findViewById(R.id.furnitureScrollTv),
                view.findViewById(R.id.carsScrollTv),
                view.findViewById(R.id.allScrollTv),
                view.findViewById(R.id.servicesScrollTv),
                view.findViewById(R.id.animalScrollTv),
                view.findViewById(R.id.personalScrollTv),
                view.findViewById(R.id.otherScrollTv)
        };
        categoryImageViews = new ImageView[]{
                view.findViewById(R.id.homeScrollIv),
                view.findViewById(R.id.mobileScrollIv),
                view.findViewById(R.id.electronicScrollIv),
                view.findViewById(R.id.furnitureScrollIv),
                view.findViewById(R.id.carsScrollIv),
                view.findViewById(R.id.allScrollIv),
                view.findViewById(R.id.servicesScrollIv),
                view.findViewById(R.id.animalScrollIv),
                view.findViewById(R.id.personalScrollIv),
                view.findViewById(R.id.otherScrollIv)
        };


        priceTextViews[0] = view.findViewById(R.id.highPriceTv);
        priceTextViews[1] = view.findViewById(R.id.lowPriceTv);


        viewsTextViews[0] = view.findViewById(R.id.lowToHighViewsTv);
        viewsTextViews[1] = view.findViewById(R.id.highToLowViewsTv);

        filterRatingBar = view.findViewById(R.id.filterRatingBar);

        filterButton = view.findViewById(R.id.filterBtn);
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

        filterButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            if (!categories.isEmpty()) {
                bundle.putStringArrayList("category", categories);
            }

            bundle.putInt("price", priceFilter);

            bundle.putInt("views", viewsFilter);
//      if (!types.isEmpty()) {
//        bundle.putStringArrayList("promoType", types);
//      }

            if (type != null && !type.isEmpty()) {

                bundle.putString("promoType", type);
            }

            bundle.putInt("date", dateFilter);
            if (filterRatingBar.getRating() != 0) {
                bundle.putDouble("rating", filterRatingBar.getRating());
            }
            dialogFragment = FilteredPromosFragment.newInstance();
            dialogFragment.setArguments(bundle);
            ((HomeActivity) getActivity()).addFragmentToHomeContainer(dialogFragment);
//      dialogFragment.show(getChildFragmentManager(), "filtered");
        });


//    typeTextViews[0].performClick();
//    filter();
//
//    typeTextViews[0].performClick();
//    categoryTextViews[0].performClick();
//    filter();
//
//    typeTextViews[0].performClick();
//    categoryTextViews[0].performClick();
//    filter();
//
//
//    typeTextViews[0].performClick();
//    categoryTextViews[0].performClick();
//    priceTextViews[0].performClick();
//    filter();
//
//    typeTextViews[0].performClick();
//    categoryTextViews[0].performClick();
//    priceTextViews[0].performClick();
//    timeTextViews[0].performClick();
//    filter();
//
//    typeTextViews[0].performClick();
//    categoryTextViews[0].performClick();
//    priceTextViews[0].performClick();
//    timeTextViews[0].performClick();
//    filterRatingBar.setRating(3);
//    filter();


    }

    void typeClickListeners() {

        for (int i = 0; i < typeTextViews.length; i++) {
            int finalI = i + 1;
            final TextView textView = typeTextViews[i];
            textView.setOnClickListener(v -> {
                if (textView.getCurrentTextColor() == textGreyColor) {
//          textView.setBackgroundResource(R.drawable.filter_red_back);
//          textView.setTextColor(textWhiteColor);

                    for (TextView textView2 : typeTextViews) {
                        textView2.setBackgroundResource(R.drawable.filter_grey_back);
                        textView2.setTextColor(textGreyColor);
                    }
                    textView.setBackgroundResource(R.drawable.filter_red_back);
                    textView.setTextColor(textWhiteColor);

                    switch (finalI) {
                        case 1:
                            type = Promotion.TEXT_TYPE;
//              addTypeToList("text");
                            break;
                        case 2:
                            type = Promotion.IMAGE_TYPE;
//              addTypeToList("image");
                            break;
                        case 3:
                            type = Promotion.VIDEO_TYPE;
//              addTypeToList("video");
                            break;
                    }

//          type = textView.getText().toString();
//          dateFilter = finalI;
                } else {
                    textView.setBackgroundResource(R.drawable.filter_grey_back);
                    textView.setTextColor(textGreyColor);

                    type = "";
//          switch (finalI) {
//            case 1:
//              types.remove("text");
//              break;
//            case 2:
//              types.remove("image");
//              break;
//            case 3:
//              types.remove("video");
//              break;
//          }
                }
            });
        }
    }

    void timeClickListeners() {

        for (int i = 0; i < timeTextViews.length; i++) {
            TextView textView = timeTextViews[i];
            int finalI = i;
            textView.setOnClickListener(v -> {
                if (textView.getCurrentTextColor() == textGreyColor) {

                    for (TextView textView2 : timeTextViews) {
                        textView2.setBackgroundResource(R.drawable.filter_grey_back);
                        textView2.setTextColor(textGreyColor);
                    }
                    textView.setBackgroundResource(R.drawable.filter_red_back);
                    textView.setTextColor(textWhiteColor);
                    dateFilter = finalI;

                } else {
                    textView.setBackgroundResource(R.drawable.filter_grey_back);
                    textView.setTextColor(textGreyColor);
                    dateFilter = 0;
                }
            });
        }
    }

    void categoryClickListeners() {

        for (int i = 0; i < categoryTextViews.length; i++) {
            final TextView textView = categoryTextViews[i];
            final ImageView imageView = categoryImageViews[i];
            textView.setOnClickListener(v -> {
                if (textView.getCurrentTextColor() == textGreyColor) {

                    textView.setBackgroundResource(R.drawable.filter_red_back);
                    textView.setTextColor(textWhiteColor);

                    DrawableCompat.setTint(
                            DrawableCompat.wrap(imageView.getDrawable()),
                            textWhiteColor
                    );
                    imageView.setBackgroundResource(R.drawable.red_circle_back);

//                    if (textView.getText().toString().equals("هواتف")) {
//                        categories.add("موبيلات");
//                        return;
//                    }
//                    if (textView.getText().toString().equals("الكترونيات")) {
//                        categories.add("اليكترونيات");
//                        return;
//                    }
                    if (textView.getText().toString().equals("الكل")) {
                        for (int j = 0; j < categoryTextViews.length; j++) {
                            TextView textView1 = categoryTextViews[j];
                            if (textView == textView1) continue;
                            textView1.setBackgroundResource(R.drawable.filter_grey_back);
                            textView1.setTextColor(textGreyColor);

                            ImageView imageView1 = categoryImageViews[j];
                            DrawableCompat.setTint(
                                    DrawableCompat.wrap(imageView1.getDrawable()),
                                    textGreyColor
                            );
                            imageView1.setBackgroundResource(R.drawable.grey_circle_back);
                        }
                        categories.clear();
                        return;
                    } else if (categoryTextViews[5].getCurrentTextColor() == textWhiteColor) {
                        TextView allTv = categoryTextViews[5];
                        ImageView allIv = categoryImageViews[5];

                        allTv.setBackgroundResource(R.drawable.filter_grey_back);
                        allTv.setTextColor(textGreyColor);

                        DrawableCompat.setTint(
                                DrawableCompat.wrap(allIv.getDrawable()),
                                textGreyColor
                        );
                        allIv.setBackgroundResource(R.drawable.grey_circle_back);
                    }


                    categories.add(textView.getText().toString());
                } else {
                    textView.setBackgroundResource(R.drawable.filter_grey_back);
                    textView.setTextColor(textGreyColor);

                    DrawableCompat.setTint(
                            DrawableCompat.wrap(imageView.getDrawable()),
                            textGreyColor
                    );
                    imageView.setBackgroundResource(R.drawable.grey_circle_back);


//                    if (textView.getText().toString().equals("هواتف")) {
//                        categories.remove("هواتف");
//                        return;
//                    }
//
//                    if (textView.getText().toString().equals("الكترونيات")) {
//                        categories.remove("اليكترونيات");
//                        return;
//                    }


                    categories.remove(textView.getText().toString());
                }
            });
            imageView.setOnClickListener(v -> textView.performClick());
        }
    }

    void priceClickListeners() {
        for (int i = 0; i < priceTextViews.length; i++) {
            TextView tv = priceTextViews[i];
            int finalI = i;
            tv.setOnClickListener(v -> {
                if (tv.getCurrentTextColor() == textGreyColor) {
                    for (TextView tv2 : priceTextViews) {
                        setTextViewBackground(tv2, R.drawable.filter_grey_back);
                        tv2.setTextColor(textGreyColor);
                    }
                    for (TextView tv2 : viewsTextViews) {
                        setTextViewBackground(tv2, R.drawable.filter_grey_back);
                        tv2.setTextColor(textGreyColor);
                    }
                    setTextViewBackground(tv, R.drawable.filter_red_back);
                    tv.setTextColor(textWhiteColor);
                    viewsFilter = 0;
                    priceFilter = finalI + 1;
                } else {
                    setTextViewBackground(tv, R.drawable.filter_grey_back);
                    tv.setTextColor(textGreyColor);
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
                if (tv.getCurrentTextColor() == textGreyColor) {
                    for (TextView tv2 : viewsTextViews) {
                        setTextViewBackground(tv2, R.drawable.filter_grey_back);
                        tv2.setTextColor(textGreyColor);
                    }
                    for (TextView tv2 : priceTextViews) {
                        setTextViewBackground(tv2, R.drawable.filter_grey_back);
                        tv2.setTextColor(textGreyColor);
                    }
                    setTextViewBackground(tv, R.drawable.filter_red_back);
                    tv.setTextColor(textWhiteColor);
                    priceFilter = 0;
                    viewsFilter = finalI + 1;
                } else {
                    setTextViewBackground(tv, R.drawable.filter_grey_back);
                    tv.setTextColor(textGreyColor);
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


    void clearAllFilters() {


        for (TextView type : typeTextViews) {
            if (type.getCurrentTextColor() == textWhiteColor) {
                type.performClick();
            }
        }

        for (TextView category : categoryTextViews) {
            if (category.getCurrentTextColor() == textWhiteColor) {
                category.performClick();
            }
        }

        for (TextView price : priceTextViews) {
            if (price.getCurrentTextColor() == textWhiteColor) {
                price.performClick();
            }
        }

        for (TextView price : priceTextViews) {
            if (price.getCurrentTextColor() == textWhiteColor) {
                price.performClick();
            }
        }

        for (TextView view : viewsTextViews) {
            if (view.getCurrentTextColor() == textWhiteColor) {
                view.performClick();
            }
        }

        for (TextView time : timeTextViews) {
            if (time.getCurrentTextColor() == textWhiteColor) {
                time.performClick();
            }
        }

        if (filterRatingBar.getRating() > 0) {
            filterRatingBar.setRating(0);
        }

    }

    private void filter() {

        filterButton.performClick();
        getActivity().getSupportFragmentManager().beginTransaction().remove(dialogFragment).commit();

    }
}