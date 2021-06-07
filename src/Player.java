import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Player {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int port;
    private String name;
    private Role role;

    Scanner scanner = new Scanner(System.in);

    public Player() {
        port = setPort();
    }


    ReadThread readThread ;
    WriteThread writeThread ;

    public void start() {
        try {
            Socket socket = new Socket("127.0.0.1",port);
            System.out.println("connected to server");

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            readThread = new ReadThread(in,this);
            writeThread = new WriteThread(out,this);

            handleName();

            handleReady();

            handleRole();

            handleIntroduction();

            startThreads();

            handleChat();


        } catch (UnknownHostException u) {
            u.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        }
    }



    Message message = null;
    String msg = "";
    private void handleName(){
        try{
            while (true){
                message = (Message) in.readObject();
                while (message == null)
                    message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);

                String pName = null;
                while (pName == null || pName.length() < 1)
                    pName = scanner.nextLine();
                out.writeObject(new Message(pName));

                message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);
                if(msg.startsWith("welcome"))
                {
                    this.name = pName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleReady(){

        try{
            while (true){
                message = (Message) in.readObject();
                while (message == null)
                    message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);

                while (!(msg.equalsIgnoreCase("ready"))){
                    msg = scanner.nextLine();
                    out.writeObject(new Message(msg));
                }

                message = (Message) in.readObject();
                msg = message.getText();
                System.out.println(msg);
                if(msg.startsWith("ok wait"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleRole() {
        Role r = null;
        while (r == null){
            try {
                r = (Role) in.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        setRole(r);

        try {
            message = (Message) in.readObject();
            msg = message.getText();
            System.out.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleIntroduction(){
        if(role instanceof Mafia || role instanceof Mayor){
            message = null;
            while (message == null){
                try {
                    message = (Message) in.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            } msg = message.getText();
            System.out.println(msg);
        }
    }

    private void startThreads() {
        writeThread.start();
        readThread.start();
    }

    private void handleChat() {
        while (readThread.getState() != Thread.State.WAITING && readThread.getState() != Thread.State.TIMED_WAITING)
            continue;
        if(writeThread.getState() != Thread.State.WAITING && writeThread.getState() != Thread.State.TIMED_WAITING){
            try {
                synchronized (writeThread){
                    writeThread.wait();
                }
            } catch (InterruptedException inter){
                inter.printStackTrace();
            }
        }
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private int setPort() {
        System.out.print("enter port: ");
        port = scanner.nextInt();
        //check statement





        return port;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        Player player = new Player();
        player.start();
    }


}
