import me.g33ry.magic_home_java.Controller;
import me.g33ry.magic_home_java.Discover;

import java.io.IOException;

public class TesterClass {

    @org.junit.Test
    public void main() throws IOException {
        Controller[] controllers = Discover.Scan();

        for (Controller controller : controllers){
            System.out.println(controller.getIP() + " ->> " + controller.getNAME());
        }

    }
}
