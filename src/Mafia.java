import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

/**
 * The type Mafia.
 */
public abstract class Mafia extends Role{

    /**
     * Instantiates a new Mafia.
     */
    public Mafia() {
        super();
    }

    @Override
    public abstract String toString();

    @Override
    public abstract String action(ObjectOutputStream out, ObjectInputStream in, Server server);

    /**
     * Mafia chat string.
     *
     * @param out    the out
     * @param in     the in
     * @param server the server
     * @return the string
     */
    public String mafiaChat(ObjectOutputStream out, ObjectInputStream in, Server server){
        boolean accepted = false;
        String target = "";
        try {

            out.writeObject(new Message("\nchoose one citizen to help godfather"));
            out.writeObject(new Message(server.getCitizenList()));

            while (!accepted){
                Message message = (Message)in.readObject();
                target = message.getText();
                String[] targetTokens = target.split(" ",2);
                if(targetTokens.length > 1)
                    target = targetTokens[1].trim();

                if(server.acceptableMafiaConsult(target)){
                    out.writeObject(new Message("accepted"));
                    accepted = true;
                }
                else
                    out.writeObject(new Message("unacceptable try again"));
            }
            System.out.println("target received " + target);

        } catch (SocketException s){

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return target;
    }
}
