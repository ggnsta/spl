import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class Server {
    protected static int serverPort = 49005;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected ClientDescriptor client_descriptor = null;
    protected Socket clientSocket = null;


    public void run() {

        //System.out.println(Thread.currentThread().getName());
        openServerSocket();
        while (true) {

            try {
                System.out.println("SERVER: Waiting.");
                clientSocket = this.serverSocket.accept(); // ждем клиента

                System.out.println("SERVER: Connection accepted.");
                client_descriptor = new ClientDescriptor(clientSocket);

                while (!(clientSocket.isClosed())) {
                    String msg = client_descriptor.get_message();
                    client_descriptor.chechExecCmd(msg);
                }

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("SERVER: Server Stopped.");
                    return;
                }
                throw new RuntimeException("SERVER: Error accepting client connection", e);
            }


            System.out.println("SERVER: Connection lost.");
        }

    }

    private void openServerSocket() {

        System.out.println("Opening server socket...");
        try {

            this.serverSocket = new ServerSocket(this.serverPort);

        } catch (ConnectException e) {
            //  ErrorNotification error = new ErrorNotification();
            //  error.eOS();
        } catch (IOException e)// (включает в себя SocketTimeoutException )
        {
            e.printStackTrace();
        }
    }

    private boolean isStopped() {
        return this.isStopped;
    }
}