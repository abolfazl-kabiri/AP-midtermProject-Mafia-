import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

/**
 * The type Mayor.
 */
public class Mayor extends Citizen{

    /**
     * Instantiates a new Mayor.
     */
    public Mayor() {
        super();
    }

    @Override
    public String toString() {
        return "Mayor";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        boolean accepted = false;
        String response = "";

        try {
            out.writeObject(new Message("do you accept this? [yes/no]"));

            while (!accepted){
                Message message = (Message) in.readObject();
                response = message.getText();

                String[] responseTokens = response.split(" ", 2);
                if(responseTokens[1] != null)
                    response = responseTokens[1];

                if(!(response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("no")))
                    out.writeObject(new Message("unacceptable try again"));
                else {
                    out.writeObject(new Message("accepted"));
                    accepted = true;
                }
            }


        } catch (SocketException s){

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }

}
