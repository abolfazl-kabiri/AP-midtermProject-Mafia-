import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Godfather extends Mafia{

    public Godfather() {
        super();
    }

    @Override
    public String toString() {
        return "Godfather";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";
        String mafiaConsult = server.gatherVotes();
        try {

            boolean accepted = false;
            out.writeObject(new Message("\nfollowing citizens are alive"));
            out.writeObject(new Message(server.getCitizenList()));
            out.writeObject(new Message(mafiaConsult));
            out.writeObject(new Message("choose one person to kill\n"));

            while (!accepted){
                Message message = (Message) in.readObject();
                response = message.getText();

                String[] targetTokens = response.split(" ",2);
                if(targetTokens.length > 1)
                    response = targetTokens[1].trim();
                if(server.acceptableMafiaConsult(response)){
                    out.writeObject(new Message("accepted"));
                    accepted = true;
                }
                else
                    out.writeObject(new Message("unacceptable try again"));
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }
}
