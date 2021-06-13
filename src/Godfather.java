import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Godfather extends Mafia{
    @Override
    public String toString() {
        return "Godfather";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";
        String mafiaConsult = server.gatherVotes();
        try {
            out.writeObject(new Message(mafiaConsult));
            out.writeObject(new Message("choose one person to kill"));

            Message message = (Message) in.readObject();
            response = message.getText();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }
}
