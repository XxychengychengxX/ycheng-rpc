import org.junit.jupiter.api.Test;

/**
 * @author Valar Morghulis
 * @Date 2023/9/2
 */

public class SystemTest {

    @Test
    public void testProperties(){
        System.out.println(System.getProperty("serveAddr"));
    }

    @Test
    public void testModuleName(){
    System.out.println("Module.class.getName() = " );
}
}
