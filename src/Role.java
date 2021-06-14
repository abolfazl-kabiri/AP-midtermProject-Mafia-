import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class Role implements Serializable {

    protected int health;

    public Role() {
        health = 1;
    }

    public void decreaseHealth(){
        this.health = health -1;
    }

    public int getHealth() {
        return health;
    }

    public abstract String toString();

    public abstract String action(ObjectOutputStream out, ObjectInputStream in, Server server);
}
