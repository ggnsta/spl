import java.net.*;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class Server {
    protected static int serverPort = 49005;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected ClientDescriptor client_descriptor = null;
    protected Socket clientSocket = null;
    protected DatagramSocket udpSocket = null;
    protected boolean UDPMODE=false;


    public void createUDPSocket()
    {
        try {
            this.udpSocket = new DatagramSocket(serverPort);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void workInUDPMode()
    {
        createUDPSocket();
        System.out.println("!!!UDP MODE ON!!!");
        UDPconnection udPconnection= new UDPconnection(udpSocket);
        while(udPconnection.UDPStatus){
            try {
                String msg = udPconnection.get();
                udPconnection.chechExecCmd(msg);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        this.UDPMODE=false;
        client_descriptor.send_message("###Now we work in TCP MODE###");
    }


    public void run()  {

        //System.out.println(Thread.currentThread().getName());

        while (true) {

            try {
                System.out.println("SERVER: Waiting.");
                clientSocket = this.serverSocket.accept(); // ждем клиента

                System.out.println("SERVER: Connection accepted.");
                client_descriptor = new ClientDescriptor(clientSocket,this);

                while (!(clientSocket.isClosed())) {
                    String msg = client_descriptor.get_message();
                    client_descriptor.chechExecCmd(msg);
                    if(UDPMODE==true)
                    {
                        workInUDPMode();
                    }
                }

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("SERVER: Server Stopped.");
                    return;
                }
                run();
               // throw new RuntimeException("SERVER: Error accepting client connection", e);
            }
           // System.out.println("SERVER: Connection lost.");
        }

    }

    public void openServerSocket() {

        System.out.println("Opening server socket...");
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (ConnectException e) {
            //  ErrorNotification error = new ErrorNotification();
            //  error.eOS();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void reconnect () throws IOException {
        System.out.println("SERVER: try to recconect.");
        clientSocket = this.serverSocket.accept(); // ждем клиента

        System.out.println("SERVER: Connection accepted.");
        client_descriptor = new ClientDescriptor(clientSocket,this);
    }
    private boolean isStopped() {
        return this.isStopped;
    }
}
