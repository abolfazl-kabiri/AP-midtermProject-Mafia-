import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SimpleMafia extends Mafia{

    public SimpleMafia() {
        super();
    }

    @Override
    public String toString() {
        return "SimpleMafia";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}
