package fresh.crafts.depwiz.services;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

@Service
public class SystemServices {

    public int randomPort(int from, int to) {
        return ThreadLocalRandom.current().nextInt(from, to);
    }

    public int nextFreePort(int from, int to) {
        int port = randomPort(from, to);
        while (true) {
            if (isPortAvailable(port)) {
                return port;
            } else {
                port = randomPort(from + 1, to - 1);
            }
        }
    }

    private static boolean isPortAvailable(int port) {
        // https://stackoverflow.com/questions/2675362/how-to-find-an-available-port
        // try (ServerSocket serverSocket = new ServerSocket(port)) {
        // // serverSocket.setReuseAddress(true);
        // serverSocket.close();
        // return true;
        // } catch (IOException e) {
        // return false;
        // }
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
