package ztest;

import java.time.Clock;
import java.util.*;

/**
 * Created by hanxuan on 9/10/15.
 */
public class Test {

    public static void main(String[] args) {
        System.out.println("hello");
        Clock c = Clock.systemUTC();
        System.out.println();
        List<String> name = Arrays.asList("baa", "1", "2s", "a");
        Collections.sort(name, (String a, String b) -> b.length() > a.length() ? 1: -1);
        System.out.println(name);
    }
}
