import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Mafia extends Role{

    @Override
    public abstract String toString();

    @Override
    public abstract String action(ObjectOutputStream out, ObjectInputStream in, Server server);

    public String mafiaChat(ObjectOutputStream out, ObjectInputStream in, Server server){
        String target = null;
        try {

            out.writeObject(new Message("\nchoose one citizen to help godfather"));
            out.writeObject(new Message(server.getCitizenList()));

            Message message = (Message)in.readObject();
            target = message.getText();
            String[] targetTokens = target.split(" ",2);
            if(targetTokens.length > 1)
                target = targetTokens[1].trim();
            System.out.println("target received " + target);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return target;
    }
}
