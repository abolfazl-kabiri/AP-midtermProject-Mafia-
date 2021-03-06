import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

/**
 * The type Detective.
 */
public class Detective extends Citizen{


    /**
     * Instantiates a new Detective.
     */
    public Detective() {
        super();
    }

    @Override
    public String toString() {
        return "Detective";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {
        boolean accepted = false;
        String response = "";

        try {
            out.writeObject(new Message("choose one person to check the role"));
            out.writeObject(new Message(server.getList()));

            while (!accepted){

                Message message = (Message) in.readObject();
                response = message.getText();

                String[] responseTokens = response.split(" ",2);
                if(responseTokens.length > 1)
                    response = responseTokens[1].trim();

                if(server.acceptableCheckRole(response)){
                    accepted = true;
                    out.writeObject(new Message("accepted"));
                }
                else
                    out.writeObject(new Message("unacceptable try again"));

            }

            Role role = server.checkRole(response);
            if(role instanceof Citizen || role instanceof Godfather)
                out.writeObject(new Message("is not mafia"));
            else
                out.writeObject(new Message("is mafia"));
        } catch (SocketException s){

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }
}
