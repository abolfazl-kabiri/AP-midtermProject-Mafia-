import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Sniper extends Citizen{
    @Override
    public String toString() {
        return "Sniper";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}
