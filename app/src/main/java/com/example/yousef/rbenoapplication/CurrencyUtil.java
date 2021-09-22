package com.example.yousef.rbenoapplication;

import java.util.Currency;
import java.util.Locale;

public class CurrencyUtil {

    public static void main(String[] args) {

        System.out.println(Currency.getInstance(Locale.getDefault()).getDisplayName());
        System.out.println(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        System.out.println(Currency.getInstance(Locale.getDefault()).getSymbol());

    }

    public static String getArabicSymbol(String currencyCode) {

        String displayName = "";

        if (currencyCode != null) {
            try {
                displayName = Currency.getInstance(currencyCode).getSymbol(new Locale("ar"));

                if (!displayName.isEmpty() && displayName.charAt(displayName.length() - 1) == '.') {
                    displayName = displayName.substring(0, displayName.length() - 1);
                }

            } catch (IllegalArgumentException e) {
                displayName = currencyCode;
            }

        }

        return displayName;

    }

}
