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
                    CheckExecCommand(msg);
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



    private void CheckExecCommand(String message)  {
        String cmd =" ";
        if (message.endsWith("\\n")||message.endsWith("\\r\\n")) {
            if (message.endsWith("\\n"))cmd = message.substring(0,message.length()-2);
            if (message.endsWith("\\r\\n"))cmd = message.substring(0,message.length()-4);

            switch (cmd) {

                case "download":{
                    cmd_accept_download();
                    break;
                }

                case "upload":{
                    cmd_accept_upload();
                    break;
                }

                case "echo": {
                    cmd_echo();
                    break;
                }
                case "stop": {
                    cmd_stop();
                    break;
                }
                case "time": {
                    cmd_time();
                    break;
                }
                default:{
                    System.out.println("Client:" + message);
                    client_descriptor.send_message(" ");
                    break;
                }
            }

        }
        else{
            System.out.println("Client:" + message);
            client_descriptor.send_message(" ");
        }
    }

    private boolean isStopped() {
        return this.isStopped;
    }

    private void stopServer() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void cmd_stop() {
        try {
            client_descriptor.send_message("Server closed connection.");
            client_descriptor.closeStreams();
            this.clientSocket.close();
            System.out.println("stop command");
        } catch (Exception e) {
            throw new RuntimeException("SERVER:Error closing server", e);
        }
    }

    private void openServerSocket() {

        System.out.println("Opening server socket...");
        try {

            this.serverSocket = new ServerSocket(this.serverPort);

        } catch (ConnectException e) {
            ErrorNotification error = new ErrorNotification();
            error.eOS();
        } catch (IOException e)// (включает в себя SocketTimeoutException )
        {
            e.printStackTrace();
        }
    }

    private ClientDescriptor establishConnection(Socket socket) {
        ClientDescriptor _client_descriptor = new ClientDescriptor(socket);
        return _client_descriptor;
    }

    public void cmd_time()
    {
        Date date = new Date();
        client_descriptor.send_message(date.toString());

    }

    public void cmd_echo()
    {
        client_descriptor.send_message("Your echo:");
        String buf = client_descriptor.get_message();
        client_descriptor.send_message(buf);

    }

    public void cmd_accept_download()  {

        System.out.println("cmd_accept_download");
        String fileName = client_descriptor.get_message();
        System.out.println(fileName);
        client_descriptor.start_upload_file(fileName);
        System.out.println("b1");
        client_descriptor.send_message("vse");
        System.out.println("b2");
       // client_descriptor.send_message("File was successfully downloaded");
    }

    public void cmd_accept_upload()  {
        System.out.println ("cmd_accept_upload");
        String fileName = client_descriptor.get_message();
        client_descriptor.start_download_file(fileName);
        System.out.println("a1");
        client_descriptor.send_message("File was successfully uploaded");
        System.out.println("a2");
    }

}