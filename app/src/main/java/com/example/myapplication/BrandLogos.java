package com.example.myapplication;

import java.util.Locale;

/** Local brand assets keep logos available even without an image download. */
public final class BrandLogos {
    private BrandLogos() {}
    public static int forName(String name) {
        String brand = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (brand.contains("kfc")) return R.drawable.logo_kfc;
        if (brand.contains("circle")) return R.drawable.logo_circlek;
        if (brand.contains("highland")) return R.drawable.ic_highland;
        if (brand.contains("eleven") || brand.contains("7-11")) return R.drawable.ic_seveneleven;
        return R.drawable.ic_food;
    }
}
