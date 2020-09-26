package com.example.yousef.rbenoapplication;

public class TimeConvertor {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }


        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "قبل قليل";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "قبل دقيقة";
        } else if (diff < 50 * MINUTE_MILLIS) {
            if (diff / MINUTE_MILLIS < 11) {
                return " قبل " + diff / MINUTE_MILLIS + " دقائق ";
            } else {
                return " قبل " + diff / MINUTE_MILLIS + " دقيقة ";
            }

        } else if (diff < 90 * MINUTE_MILLIS) {
            return "قبل ساعة";
        } else if (diff / HOUR_MILLIS == 2) {
            return "قبل ساعتين";

        } else if (diff < 24 * HOUR_MILLIS) {
            if (diff / HOUR_MILLIS < 11 && diff / HOUR_MILLIS > 1 && diff / HOUR_MILLIS > 2) {
                return " قبل " + diff / HOUR_MILLIS + " ساعات ";
            } else {
                return " قبل " + diff / HOUR_MILLIS + " ساعة ";
            }
        } else if (diff < 48 * HOUR_MILLIS) {
            return "الأمس";
        } else {
            if (diff / DAY_MILLIS > 7 && diff / DAY_MILLIS < 14) {
                return "قبل اسبوع";
            } else if (diff / DAY_MILLIS > 14 && diff / DAY_MILLIS < 21) {
                return "قبل اسبوعين";
            } else if (diff / DAY_MILLIS > 21 && diff / DAY_MILLIS < 30) {
                return "قبل ثلاثة اسابيع";
            } else if (diff / DAY_MILLIS > 30 && diff / DAY_MILLIS < 60) {
                return "قبل شهر";
            } else if (diff / DAY_MILLIS > 60 && diff / DAY_MILLIS < 90) {
                return "قبل شهرين";
            } else if (diff / DAY_MILLIS > 90 && diff / DAY_MILLIS < 120) {
                return "قبل ثلاثة أشهر";
            } else if (diff / DAY_MILLIS > 120 && diff / DAY_MILLIS < 150) {
                return "قبل أربعة أشهر";
            } else if (diff / DAY_MILLIS > 150 && diff / DAY_MILLIS < 180) {
                return "قبل خمسة أشهر";
            } else if (diff / DAY_MILLIS > 180 && diff / DAY_MILLIS < 210) {
                return "قبل ستة أشهر";
            } else if (diff / DAY_MILLIS > 210 && diff / DAY_MILLIS < 240) {
                return "قبل سبعة أشهر";
            } else if (diff / DAY_MILLIS > 240 && diff / DAY_MILLIS < 270) {
                return "قبل ثمانية أشهر";
            } else if (diff / DAY_MILLIS > 270 && diff / DAY_MILLIS < 300) {
                return "قبل تسعة أشهر";
            } else if (diff / DAY_MILLIS > 300 && diff / DAY_MILLIS < 330) {
                return "قبل عشرة أشهر";
            } else if (diff / DAY_MILLIS > 330 && diff / DAY_MILLIS < 365) {
                return "قبل إحدى عشرة شهر";
            } else {
                return " قبل " + diff / DAY_MILLIS + " أيام ";
            }
        }
    }

    static long getTimeDifference(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return 0;
        }


        return now - time;
    }


}