package io.goji.jav.jep408.WebServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import com.sun.net.httpserver.SimpleFileServer;

import static java.nio.file.StandardOpenOption.CREATE;

/*
 * A Simple Web Server that serves the contents of a zip file system.
 */
public class SWSZipFS {
    private static final InetSocketAddress LOOPBACK_ADDR =
            new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080);
    static final Path CWD = Path.of(".").toAbsolutePath();

    /* Creates a zip file system and starts a Simple Web Server to serve its
     * contents.
     *
     * Upon receiving a GET request, the server sends a response with a status
     * of 200 if the relative URL matches /someFile.txt, otherwise a 404 response
     * is sent. Query parameters are ignored.
     * The body of the response will be the content of the file "Hello world!"
     * and a Content-type header will be sent with a value of "text/plain".
     */
    public static void main( String[] args ) throws Exception {
        Path root = createZipFileSystem();
        var server = SimpleFileServer.createFileServer(LOOPBACK_ADDR, root, SimpleFileServer.OutputLevel.VERBOSE);
        server.start();
    }

    private static Path createZipFileSystem() throws Exception {
        var path = CWD.resolve("zipFS.zip").toAbsolutePath().normalize();
        var fs = FileSystems.newFileSystem(path, Map.of("create", "true"));
        assert fs != FileSystems.getDefault();
        var root = fs.getPath("/");  // root entry

        /* Create zip file system:
         *    |-- root
         *        |-- aFile.txt
         */

        Files.writeString(root.resolve("someFile.txt"), "Hello world!", CREATE);
        return root;
    }
}
