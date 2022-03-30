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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CreateRating {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
     * using "curl" command in bash:
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
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        JSONObject json = new JSONObject(StringEscapeUtils.unescapeJava(request.getBody().get()));
        id = UUID.randomUUID().toString();
        userId = json.getString("userId");
        productId = json.getString("productId");
        rating = json.getInt("rating");
        locationName = json.getString("locationName");
        userNotes = json.getString("userNotes");
        timeStamp = LocalDateTime.now().toString();

        // https://serverlessohapi.azurewebsites.net/api/GetUser?userId=cc20a6fb-a91f-4192-874d-132493685376
        String userURL = "https://serverlessohapi.azurewebsites.net/api/GetUser?userId=" + userId;
        boolean isUserValid = false;
        
        String productURL = "https://serverlessohapi.azurewebsites.net/api/GetProduct?productId=" + productId;
        boolean isProductValid = false;
      
        try {
            URL userIdUrl = new URL(userURL);
            isUserValid = getResponse(userIdUrl,context);

            URL productIdUrl = new URL(productURL);
            isProductValid = getResponse(productIdUrl,context);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        context.getLogger().info("Valid User "+isUserValid+ " Valid Prouduct "+isProductValid);

        if (json == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(createJson().toString()).build();
        }
    }

    public boolean isValid() {
        return false;
    }

    private JSONObject createJson() {
        JSONObject responseJson = new JSONObject();
        responseJson.put("id", id);
        responseJson.put("userId", userId);
        responseJson.put("productId", productId);
        responseJson.put("timestamp", timeStamp);
        responseJson.put("locationName", locationName);
        responseJson.put("rating", rating);
        responseJson.put("userNotes", userNotes);
        return responseJson;
    }

    private boolean getResponse(URL url,ExecutionContext context) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);

        connection.connect();
        int code = connection.getResponseCode();
        context.getLogger().info("URL " +url);
        context.getLogger().info("Response Code" +connection.getResponseCode());

        if (code == 200) {
            return true;
        } 

        return false;
    }
}
