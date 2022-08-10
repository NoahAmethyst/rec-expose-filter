package com.oye.ref.utils;

import java.util.UUID;

public class GenerateUtil {

    public static String duang() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
