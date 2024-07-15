package io.goji.jav.TimeB;

import javax.swing.text.DateFormatter;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HexFormat;

public class Main {
    public static void main(String[] args) {
        String s = DateTimeFormatter.ofPattern("y-M-d H:m:s").format(LocalDateTime.now());
        System.out.println(s);


        HexFormat format = HexFormat.of();

        byte[] input = new byte[] {127, 0, -50, 105};
        String hex = format.formatHex(input);
        System.out.println(hex);

        byte[] output = format.parseHex(hex);
        assert Arrays.compare(input, output) == 0;
    }
}
