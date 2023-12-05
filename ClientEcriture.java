import java.io.*;
import java.net.Socket;

public class ClientEcriture {
    public static void main(String[] args) {
        try {
        Socket socket = new Socket("127.0.0.1",8080);
        PrintWriter writer =
        new PrintWriter(socket.getOutputStream());
        writer.println("Hellow world!");
        writer.flush();
        socket.close();
        }catch (Exception e) {
        e.printStackTrace();
        }
    }
}