package ca.utoronto.utm.mcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Utils {
    public static String convert(InputStream inputStream) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public static String actoridchanger(String id){
        int len = id.length();
        String newid = id;
        while (len != 7){
            newid = "0" +newid;
            len += 1;
        }
        return ("nm"+newid);
    }
    public static String movieidchanger(String id){
        int len = id.length();
        String newid2 = id;
        while (len != 7){
            newid2 = "0" +newid2;
            len += 1;
        }
        return ("tt"+newid2);
    }

}