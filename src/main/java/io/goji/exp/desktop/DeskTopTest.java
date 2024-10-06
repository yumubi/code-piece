package io.goji.exp.desktop;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DeskTopTest {
    public static void main(String[] args) {
        Desktop desktop = Desktop.getDesktop();

        URI uri = null;
        try {
            uri = new URI("https://www.pixiv.net/");
            
            desktop.browse(uri);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
