package com.awesome.towerdefense;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

public class FontCache {
    private static Hashtable<String, Typeface> fontCache = new Hashtable<>();

    public static Typeface get(String fontFileName, Context context) {
        Typeface tf = fontCache.get(fontFileName);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontFileName);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
            fontCache.put(fontFileName, tf);
        }
        return tf;
    }
}
