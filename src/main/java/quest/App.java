package quest;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import quest.model.Message;
import quest.model.Motto;
import quest.model.User;
import quest.services.Utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.*;

/**
 * @author A. Aleynikov, M. Chernenko, A. Kholmov, A. Kotov
 */
public class App {

    private static final String teamId = "1";

    public static String getFirstWallResult() {

        final int mottoCount = 221;

        Client client = Utils.getClient();
        WebResource service = client.resource(UriBuilder.fromUri("http://192.168.88.64:8080/motto").build());

        int min = 0;
        for (int i = min + 1; i < mottoCount; i++) {

            ClientResponse response = service.type(MediaType.APPLICATION_JSON)
                    .header("team-id", teamId)
                    .post(ClientResponse.class, "{ \"motto1\":" + min + ", \"motto2\":" + i + " }");

            Integer answer = response.getEntity(Integer.class);
            System.out.println("Response ("+min+", "+i+")" + answer);
            if ( answer > -1 ) {
                min = i;
            }
        }

        service = client.resource(UriBuilder.fromUri("http://192.168.88.64:8080/motto/" + min).build());
        ClientResponse response = service.type(MediaType.APPLICATION_JSON)
                .header("team-id", teamId)
                .get(ClientResponse.class);

        String answerString = response.getEntity(String.class);

        Gson gs = new Gson();
        return gs.fromJson(answerString, Motto.class).getMotto();

    }

    public static String getSecondWallResult(String keyword) {

        final int puzzleCount = 2081;

        Client client = Utils.getClient();

        int firstPosition = 0;
        int secondPosition = 0;

        for (int i = 0; i < puzzleCount; i++) {

            WebResource service = client.resource(UriBuilder.fromUri("http://192.168.88.64:8080/puzzle/" + i).build());
            ClientResponse response = service.type(MediaType.APPLICATION_JSON)
                    .header("team-id", teamId)
                    .post(ClientResponse.class, "{ }");

            String firstValue = response.getHeaders().get("direction-one-to-go").get(0);
            String secondValue = response.getHeaders().get("direction-two-to-go").get(0);
            if (firstValue.equals("0")) {
                firstPosition = i;
            } else if (secondValue.equals("0")) {
                secondPosition = i;
            }
        }

        int keyPosition = firstPosition / 2 + secondPosition / 2;

        WebResource service = client.resource(UriBuilder.fromUri("http://192.168.88.64:8080/puzzle/" + keyPosition).build());
        ClientResponse response = service.type(MediaType.APPLICATION_JSON)
                .header("team-id", teamId)
                .get(ClientResponse.class);

        String answerString = response.getEntity(String.class);

        Gson gs = new Gson();
        return gs.fromJson(answerString, Message.class).getMessage();

    }

    public static List<?> getThirdWallResult(String keyword) {

        final int keyLength = 4;

        Client client = Utils.getClient();

        WebResource service = client.resource(UriBuilder.fromUri("http://192.168.88.64:8080/users").build());
        ClientResponse response = service.type(MediaType.APPLICATION_JSON)
                .header("team-id", teamId)
                .header("keyword", keyword)
                .get(ClientResponse.class);

        Gson gs = new Gson();
        User[] users = gs.fromJson(response.getEntity(String.class), User[].class);

        Map<String, Integer> letterMap = new HashMap<>();
        for(User user: users) {
            if (user.getGender().equals("female")) {

                service = client.resource(UriBuilder.fromUri("http://192.168.88.64:8080/users/" + user.getName()).build());
                response = service.type(MediaType.APPLICATION_JSON)
                        .header("team-id", teamId)
                        .header("keyword", keyword)
                        .get(ClientResponse.class);

                String responceString = response.getEntity(String.class);
                User fullUser = gs.fromJson(responceString, User.class);

                if (fullUser.getTribe().equals("hobbits")) {
                    String name = fullUser.getName().toLowerCase();
                    for (int i = 0; i < name.length(); i++) {
                        String letter = name.substring(i, i + 1);
                        Integer count = letterMap.get(letter);
                        if (count == null) {
                            count = 0;
                        }
                        count++ ;
                        letterMap.put(letter, count);
                    }
                }
            }
        }

        List<?> mapEntries = Arrays.asList(letterMap.entrySet());

        mapEntries.sort((o1, o2) -> {
            if (letterMap.get(o1.toString()) >= letterMap.get(o2.toString())) {
                return -1;
            } else {
                return 1;
            }
        });

        return mapEntries;
    }

    public static void main(String[] args) {

        /*try {
            Utils.startServer();
        } catch (ServletException | LifecycleException e) {
            e.printStackTrace();
        }*/

        // first step
        String keyword = getFirstWallResult();
        System.out.println(keyword);

        // second step
        String puzzle = getSecondWallResult(keyword);
        System.out.println(puzzle);
        // puzzle -> BRAIN -> "pentagon"
        keyword = "pentagon";

        // third step
        List<?> keyValuePairList = getThirdWallResult(keyword);
        System.out.println(keyValuePairList);
        // sorted array of letters -> BRAIN -> "aidl"
        keyword = "aidl";

        System.out.println(keyword);

        // final step
        // WebService's work

    }

}
