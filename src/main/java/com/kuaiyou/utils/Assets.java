package com.kuaiyou.utils;

import android.util.Base64;

public class Assets {

    public final static String NATIVEVIDEOVAST = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiIHN0YW5kYWxvbmU9InllcyI/Pgo8VkFTVCB2ZXJzaW9uPSIzLjAiPgogICAgPEFkPgogICAgICAgIDxJbkxpbmU+CiAgICAgICAgICAgIDxBZFN5c3RlbS8+CiAgICAgICAgICAgIDxBZFRpdGxlPgogICAgICAgICAgICA8L0FkVGl0bGU+CiAgICAgICAgICAgICAgIF9fSU1QUkVTU0lPTl9fCiAgICAgICAgICAgIDxDcmVhdGl2ZXM+CiAgICAgICAgICAgICAgICA8Q3JlYXRpdmU+CiAgICAgICAgICAgICAgICAgICAgPExpbmVhcj4KICAgICAgICAgICAgICAgICAgICAgICAgX19EVVJBVElPTl9fCiAgICAgICAgICAgICAgICAgICAgICAgIDxUcmFja2luZ0V2ZW50cz4KICAgICAgICAgICAgICAgICAgICAgICAgICAgIF9fU1RBUlRfRVZFTlRfXwogICAgICAgICAgICAgICAgICAgICAgICAgICAgX19NSURETEVfRVZFTlRfXwogICAgICAgICAgICAgICAgICAgICAgICAgICAgX19FTkRfRVZFTlRfXwogICAgICAgICAgICAgICAgICAgICAgICA8L1RyYWNraW5nRXZlbnRzPgogICAgICAgICAgICAgICAgICAgICAgICA8VmlkZW9DbGlja3M+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICBfX0NMSUNLVEhST1VHSFRfXwogICAgICAgICAgICAgICAgICAgICAgICAgICAgX19DTElDS1RSQUNLSU5HX18KICAgICAgICAgICAgICAgICAgICAgICAgPC9WaWRlb0NsaWNrcz4KICAgICAgICAgICAgICAgICAgICAgICAgPE1lZGlhRmlsZXM+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICBfX01FRElBRklMRV9fCiAgICAgICAgICAgICAgICAgICAgICAgIDwvTWVkaWFGaWxlcz4KICAgICAgICAgICAgICAgICAgICA8L0xpbmVhcj4KICAgICAgICAgICAgICAgIDwvQ3JlYXRpdmU+CiAgICAgICAgICAgIDwvQ3JlYXRpdmVzPgogICAgICAgICAgICA8RXh0ZW5zaW9ucz4KICAgICAgICAgICAgICAgIDxFeHRlbnNpb24+CiAgICAgICAgICAgICAgICAgICAgX19FWFRFTlNJT05fXwogICAgICAgICAgICAgICAgPC9FeHRlbnNpb24+CiAgICAgICAgICAgIDwvRXh0ZW5zaW9ucz4KICAgICAgICA8L0luTGluZT4KICAgIDwvQWQ+CjwvVkFTVD4K";

    public static String getJsFromBase64(String encodedString) {
        byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
        return new String(decodedString);
    }
}
