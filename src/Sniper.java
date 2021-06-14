import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Sniper extends Citizen{

    public Sniper() {
        super();
    }

    @Override
    public String toString() {
        return "Sniper";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {
        String response = "";
        try {
                boolean accepted = false;

                out.writeObject(new Message("following players are alive"));
                out.writeObject(new Message("choose one player or write \"no\""));
                out.writeObject(new Message(server.getList()));

                while (!accepted){
                    Message message = (Message) in.readObject();
                    response = message.getText();

                    String[] responseTokens = response.split(" ",2);
                    if(responseTokens.length > 1)
                        response = responseTokens[1].trim();

                    if(server.acceptableSniperShot(response)){
                        accepted = true;
                        out.writeObject(new Message("accepted"));
                    }
                    else
                        out.writeObject("unacceptable try again");

                }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
