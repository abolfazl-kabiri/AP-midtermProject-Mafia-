import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DrLector extends Mafia{
    @Override
    public String toString() {
        return "DrLecter";
    }

    @Override
    public String  action(ObjectOutputStream out, ObjectInputStream in, Server server) {
        String response = "";

        try {
            out.writeObject(new Message("\nchoose one of your teammates to heal"));
            out.writeObject(new Message("you can heal yourself just 1 time"));
            out.writeObject(new Message("following mafias are alive"));
            out.writeObject(new Message(server.getMafiaList()));

            Message message = (Message) in.readObject();
            response = message.getText();

            String[] responseTokens = response.split(" ",2);
            if(responseTokens.length > 1)
                response = responseTokens[1].trim();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }
}
