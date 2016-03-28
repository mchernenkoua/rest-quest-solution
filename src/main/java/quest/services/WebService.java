package quest.services;


import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import quest.model.Message;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * @author A. Aleynikov, M. Chernenko, A. Kotov, A. Kholmov
 */
@Path("/")
@Consumes("application/json")
public class WebService {


	@POST
	public void processMessage(String stringMessage) {

		//System.out.println(stringMessage);

		Client client = Utils.getClient();
		WebResource service = client.resource(UriBuilder.fromUri("http://192.168.88.64:8080/winnersRegistration").build());

		Gson gs = new Gson();
		Message message = gs.fromJson(stringMessage, Message.class);

		//System.out.println(message.getKeyword());

        ClientResponse response = service.type(MediaType.APPLICATION_JSON)
				.header("team-id", "2")
				.header("keyword", message.getKeyword())
				.post(ClientResponse.class, "{ \"teamUrl\": " + " \"http://192.168.88.140:8080/api/\" "
											+ " }");

        String answer = response.getEntity(String.class);
		System.out.print(answer);

	}

}
