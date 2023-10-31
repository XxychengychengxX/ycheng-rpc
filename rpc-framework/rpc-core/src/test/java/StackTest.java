import org.junit.jupiter.api.Test;

import java.util.Stack;

/**
 * @author Valar Morghulis
 * @Date 2023/9/22
 */
public class StackTest {
    @Test
    public void testStack(){
        Stack<Character> characters = new Stack<>();
        characters.push('1');
        System.out.println(characters.pop());
    }
}
