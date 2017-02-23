package org.kow.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kow.domain.User;

import java.io.IOException;

public class POWScraper {
    private static String powUrl = "https://playoverwatch.com/ko-kr/career/pc/kr/";

    public static User getUser(String battleTag) throws IOException {
        Document doc = Jsoup.connect(powUrl + battleTag).get();
        
    }
}
