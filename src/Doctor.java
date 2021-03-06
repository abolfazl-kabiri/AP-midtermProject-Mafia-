import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

/**
 * The type Doctor.
 */
public class Doctor extends Citizen{

    /**
     * Instantiates a new Doctor.
     */
    public Doctor() {
        super();
    }

    @Override
    public String toString() {
        return "Doctor";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {
        boolean accepted = false;
        String response = "";

        try {
            out.writeObject(new Message("\nchoose one of players to heal"));
            out.writeObject(new Message("you can heal yourself just 1 time"));
            out.writeObject(new Message("following players are alive"));
            out.writeObject(new Message(server.getList()));


            while (!accepted){
                Message message = (Message) in.readObject();
                response = message.getText();

                String[] responseTokens = response.split(" ",2);
                if(responseTokens.length > 1)
                    response = responseTokens[1].trim();
                if(server.acceptableDoctorHeal(response)){
                    out.writeObject(new Message("accepted"));
                    accepted = true;
                }
                else
                    out.writeObject(new Message("unacceptable try again"));
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
