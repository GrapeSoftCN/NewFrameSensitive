package Test;
import common.java.httpServer.booter;
import common.java.nlogger.nlogger;

/**
 * Created by Administrator on 2018/4/3/003.
 */
public class TestWords {
    public static void main(String[] args) {

        booter booter = new booter();
        try {
            System.out.println("GrapeSensitive");
            System.setProperty("AppName", "GrapeSensitive");
            booter.start(1059);
        } catch (Exception e) {
            nlogger.logout(e);
        }


    }
}
