package com.product.rating;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CreateRating {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    private String id;
    private String userId;
    private String productId;
    private String timeStamp;
    private String locationName;
    private int rating;
    private String userNotes;

    @FunctionName("createRating")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        //CreateRating rating = new CreateRating("demo","demo","demo","demo","demo",5,"demo");
        // Parse query parameter
       JSONObject json = new JSONObject(StringEscapeUtils.unescapeJava(request.getBody().get()));

        // CreateRating rating = request.getBody();
        final String query = request.getQueryParameters().get("name");
        //final String name = request.getBody().orElse(query);

        if (json == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(json).build();
        }
    }

    public boolean isValid(){
        return false;
    }
}
