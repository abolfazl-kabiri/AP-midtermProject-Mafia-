import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The type Role.
 */
public abstract class Role implements Serializable {

    /**
     * The Health.
     */
    protected int health;

    /**
     * Instantiates a new Role.
     */
    public Role() {
        health = 1;
    }

    /**
     * Decrease health.
     */
    public void decreaseHealth(){
        this.health = health -1;
    }

    /**
     * Gets health.
     *
     * @return the health
     */
    public int getHealth() {
        return health;
    }

    public abstract String toString();

    /**
     * Action string.
     *
     * @param out    the out
     * @param in     the in
     * @param server the server
     * @return the string
     */
    public abstract String action(ObjectOutputStream out, ObjectInputStream in, Server server);
}
