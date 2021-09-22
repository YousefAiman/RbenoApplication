package com.example.yousef.rbenoapplication;

import java.util.Locale;

public class CountryUtil {

    public static String getCountryName(String countryCode) {

        String country = null;
        if (countryCode != null) {

            country = new Locale(Locale.getDefault().getLanguage(),
                    countryCode).getDisplayCountry(new Locale("ar"));

            if (country.split(" ").length > 2) {
                if (country.equals("المملكة العربية السعودية")) {
                    country = "السعودية";
                } else {
                    country = country.substring(country.lastIndexOf(" ") + 1);
                }
            }

        } else if (country != null) {
            String code = getCountryCode(country, "en");
            if (code == null) {
                code = getCountryCode(country, "ar");
            }
            if (code == null) {
                code = getCountryCode(country, Locale.getDefault().getLanguage());
            }

            country = new Locale(code).getDisplayCountry();

        }

        return country;
    }

    public static void main(String[] args) {

        System.out.println("getCountryName(\"ps\"): " + getCountryName("ps"));
        System.out.println(EmojiUtil.countryCodeToEmoji("ps"));

    }

    private static String getCountryCode(String countryName, String language) {

        String[] isoCountryCodes = Locale.getISOCountries();
        Locale locale;
        String name;

        for (String code : isoCountryCodes) {
            locale = new Locale(language, code);
            name = locale.getDisplayCountry(locale);

            if (name.equals(countryName)) {
                return code;
            }
        }

        return null;
    }

}
