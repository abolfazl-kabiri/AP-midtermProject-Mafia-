import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

/**
 * The type Psychologist.
 */
public class Psychologist extends Citizen{

    /**
     * Instantiates a new Psychologist.
     */
    public Psychologist() {
        super();
    }

    @Override
    public String toString() {
        return "Psychologist";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {
        boolean accepted = false;
        String response = "";

        try {
            out.writeObject(new Message("\nfollowing players are alive"));
            out.writeObject(new Message("choose one to mute or write \"no\""));
            out.writeObject(new Message(server.getList()));

            while (!accepted){
                Message message = (Message) in.readObject();
                response = message.getText();

                String[] responseTokens = response.split(" ",2);
                if(responseTokens.length > 1)
                    response = responseTokens[1].trim();

                if(server.acceptablePsychoChoice(response)){
                    accepted = true;
                    out.writeObject(new Message("accepted"));
                }
                else
                    out.writeObject(new Message("unacceptable try again"));

            }
        } catch (SocketException s){

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }


        return response;
    }
}
