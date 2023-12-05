import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
        Socket socket = new Socket("127.0.0.1",8080);
        InputStreamReader stream =
        new InputStreamReader(socket.getInputStream());
        BufferedReader reader = new BufferedReader(stream);
        String message = reader.readLine();
        socket.close();
        }catch (Exception e) {
        e.printStackTrace();
        }
    }
}