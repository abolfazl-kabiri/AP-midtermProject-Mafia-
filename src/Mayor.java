import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;

public class Mayor extends Citizen{
    @Override
    public String toString() {
        return "Mayor";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {
        Message message = null;
        String response = "";
        try {
            out.writeObject(new Message("do you accept this? [yes/no]"));

            while (message == null){
                message = (Message) in.readObject();
                response = message.getText();
            }
            String[] responseTokens = response.split(" ", 2);
            if(responseTokens[1] != null)
                response = responseTokens[1];
            else
                response = "yes";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }

}
