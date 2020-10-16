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



    private void CheckExecCommand(String message) {
        if (message.endsWith("\\n")||message.endsWith("\\r\\n")) {
            String cmd = message.substring(0, message.length() - 2);
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

    public void cmd_accept_upload(){
        client_descriptor.acceptUpload();
        String fileName = client_descriptor.get_message();
        long fileSize= Long.valueOf(client_descriptor.get_message());
        System.out.println(fileName);
        System.out.println(fileSize);
        client_descriptor.downloadFile(fileName,fileSize);
        client_descriptor.send_message("File was successfully uploaded");
    }

    public void cmd_accept_download()
    {
        String fileName = client_descriptor.get_message();
        client_descriptor.acceptDownload(fileName);
    }

}