import java.util.Stack;

public class ParseJson {
    public static void main(String[] args) {
        // String json =
        // "{\"id\":\"01J3A090B2BQV1Z87JE23XC2X7\",\"eventSource\":\"ENGINE\",\"payload\":{\"dbModelId\":\"01J3A090ASF3YFK1KWDYEP9703\",\"command\":\"CREATE_USER_AND_DB\",\"dbName\":\"test__0\",\"dbUser\":\"test__0\",\"dbPassword\":\"test__0\",}}";
        String json = "{\"id\":\"01J3A090B2BQV1Z87JE23XC2X7\",\"eventSource\":\"ENGINE\"}";

        System.out.println("===========================");
        Stack<String> jstack = new Stack<>();

        for (int i = 0; i < json.length(); i++) {
            char charx = json.charAt(i);
            System.out.println(charx + " " + i);
            if (charx == '{') {
                System.out.println("put to stack" + "{");
            }

        }
        System.out.println();
        System.out.println("===========================");
        System.out.println("===========================");
        System.out.println("===========================");
    }
}