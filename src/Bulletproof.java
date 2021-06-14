import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Bulletproof extends Citizen{

    private int remainingChecks;

    public Bulletproof() {
        this.health = 2;
        remainingChecks = 2;
    }

    @Override
    public String toString() {
        return "Bulletproof";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {
        boolean accepted = false;
        String response = "";

        try {
            if(remainingChecks > 0)
            {
                out.writeObject(new Message("do you want check killed roles?[yes/no]"));
                out.writeObject(new Message("you can do it just 2 times"));
                while (!accepted){

                    Message message = (Message) in.readObject();
                    response = message.getText();

                    String[] responseTokens = response.split(" ",2);
                    if(responseTokens.length > 1)
                        response = responseTokens[1].trim();

                    if(!(response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("no")))
                        out.writeObject(new Message("unacceptable try again"));
                    else if(response.equalsIgnoreCase("yes")){
                        accepted = true;
                        out.writeObject(new Message("accepted"));
                        out.writeObject(new Message("it will be sent in next day"));
                        remainingChecks--;
                    } else {
                        accepted = true;
                        out.writeObject(new Message("accepted"));
                    }
                }
            } else {
                out.writeObject(new Message("you are out of enquiry"));
                response = "no";
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
