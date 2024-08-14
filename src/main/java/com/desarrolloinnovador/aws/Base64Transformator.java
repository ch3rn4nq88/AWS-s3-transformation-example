package com.desarrolloinnovador.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.WriteGetObjectResponseRequest;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class Base64Transformator {

    public void handleRequest(S3ObjectLambdaEvent event, Context context) throws Exception {
        LambdaLogger logger = context.getLogger();
        AmazonS3 s3Client = AmazonS3Client.builder().build();
        HttpClient httpClient = HttpClient.newBuilder().build();

        var presignedResponse = httpClient.send(
                HttpRequest.newBuilder(new URI(event.inputS3Url())).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream());

        logger.log("Presigned response recovered");

        var result= IOUtils.toString(
                        presignedResponse.body());
        var encoded= new String(Base64.getEncoder().encode(result.getBytes()));

        logger.log("Presigned response encoded to base 64");

        s3Client.writeGetObjectResponse(new WriteGetObjectResponseRequest()
                .withRequestRoute(event.outputRoute())
                .withRequestToken(event.outputToken())
                .withInputStream(new ByteArrayInputStream(encoded.getBytes())));

        logger.log("Outputstream written");
    }
}
