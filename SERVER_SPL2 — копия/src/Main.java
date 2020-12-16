public class Main {
    public static void main(String[] args) {
        Server server = new Server( );
        server.openServerSocket();
        server.run();
    }
}
