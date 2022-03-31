package com.product.rating;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
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
import java.util.ArrayList;
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

    private final String databaseName = "RatingDetails";
    private final String containerName = "RatingApp";

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer container;


    @FunctionName("createRating")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                    @CosmosDBOutput(name = "RatingDetails",
              databaseName = "RatingApp",
              collectionName = "Rating",
              connectionStringSetting = "CONNECTION_STRING")
              OutputBinding<Rating> outputItem,
            final ExecutionContext context) throws Exception{
        context.getLogger().info("Java HTTP trigger processed a request.");

        JSONObject json = new JSONObject(StringEscapeUtils.unescapeJava(request.getBody().get()));
        id = UUID.randomUUID().toString();
        userId = json.getString("userId");
        productId = json.getString("productId");
        rating = json.getInt("rating");
        locationName = json.getString("locationName");
        userNotes = json.getString("userNotes");
        timeStamp = LocalDateTime.now().toString();


        String userURL = "https://serverlessohapi.azurewebsites.net/api/GetUser?userId=" + userId;
        boolean isUserValid = false;

        String productURL = "https://serverlessohapi.azurewebsites.net/api/GetProduct?productId=" + productId;
        boolean isProductValid = false;

        try {
            URL userIdUrl = new URL(userURL);
            isUserValid = getResponse(userIdUrl, context);

            URL productIdUrl = new URL(productURL);
            isProductValid = getResponse(productIdUrl, context);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        context.getLogger().info("Valid User " + isUserValid + " Valid Prouduct " + isProductValid);

        if (!isUserValid) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid User").build();
        } else if (!isProductValid) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid Product").build();
        } else if (!isRatingValid()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid Rating").build();
        } else if (isUserValid && isProductValid && isRatingValid()) {
           // createData();
            Rating ratingPojo = new Rating(id, userId, productId, timeStamp, locationName, rating, userNotes);
            outputItem.setValue(ratingPojo);
            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json")
                    .body(createJson().toString()).build();

        } else {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please check the Request").build();
        }
    }

    public boolean isRatingValid() {
        if (rating >= 0 && rating <= 5) {
            return true;
        }
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

    private boolean getResponse(URL url, ExecutionContext context) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);

        connection.connect();
        int code = connection.getResponseCode();
        context.getLogger().info("URL " + url);
        context.getLogger().info("Response Code" + connection.getResponseCode());

        if (code == 200) {
            return true;
        }

        return false;
    }

    private void createData() throws Exception {
        Rating ratingPojo = new Rating(id, userId, productId, timeStamp, locationName, rating, userNotes);
                ArrayList<String> preferredRegions = new ArrayList<String>();
                preferredRegions.add("West US");

                //  Create sync client
                client = new CosmosClientBuilder()
                        .endpoint("https://ratingappcosmojava.documents.azure.com:443/")
                        .key("2EqYFw0i19Mq5zagr3UcTrxIkAaRZWbCR6tPJv6Iql7S6EnqR2nBLEBKlRH4EF1SLZPlQLtQuUtjzQmsoEMb9w==")
                        .preferredRegions(preferredRegions)
                        .userAgentSuffix("RatingAppCosmosJava")
                        .consistencyLevel(ConsistencyLevel.EVENTUAL)
                        .buildClient();

                createDatabaseIfNotExists();
                createContainerIfNotExists();
                createRating(ratingPojo);

    }

    private void createDatabaseIfNotExists() throws Exception {
        //System.out.println("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());

        //System.out.println("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExists() throws Exception {
        //System.out.println("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/partitionKey");

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        container = database.getContainer(containerResponse.getProperties().getId());

    }

    private void createRating(Rating rating) throws Exception {

            //  Create item using container that we created using sync client

            //  Using appropriate partition key improves the performance of database operations
            CosmosItemResponse item = container.createItem(rating, null, new CosmosItemRequestOptions());
    }

}
