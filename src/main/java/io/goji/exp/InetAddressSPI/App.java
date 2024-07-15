package io.goji.exp.InetAddressSPI;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class App {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName("goji");
        for (InetAddress address : addresses) {
            System.out.println(address);
        }
        InetAddress byAddress = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        System.out.println(byAddress.getHostName());
    }
}
