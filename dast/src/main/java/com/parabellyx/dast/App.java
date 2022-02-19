// Code based developed from https://www.zaproxy.org/docs/api/
// Detailed instructions can be found at the above URL.

package com.parabellyx.dast;

import java.io.*;
import java.net.URLEncoder;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;

public class App 
{

    private static final String ZAP_ADDRESS = "localhost";
    private static final int ZAP_PORT = 9191;
    private static final String ZAP_API_KEY = System.getenv("ZAP_KEY");
    private static final String contextId = "1";
    private static final String contextName = "DVWA";
    private static final String target = System.getenv("TARGET_URL");

    private static void setIncludeInContext(ClientApi clientApi) throws UnsupportedEncodingException, ClientApiException {
        String includeInContext = System.getenv("INCLUDEINCONTEXT");

        clientApi.context.includeInContext(contextName, includeInContext);
    }

    private static void writeReport(byte[] bytes){
        try {
            Path path = Paths.get("./report.html");
            Files.write(path, bytes);
        } catch (Exception e) {

        }
    }

    private static void stopZap(ClientApi clientApi) throws ClientApiException{
        System.out.println("Stoping Zap");
        clientApi.core.shutdown();
    }

    public static void main( String[] args )
    {
        if (args.length != 1) { 
            System.exit(1);
        } 

        if(args[0].equals("init")){
            try {
                System.out.println("initializing zap");
                ClientApi clientApi = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);
                setIncludeInContext(clientApi);
            } catch (Exception e){

            }
        }

        if(args[0].equals("spider")){
            try{
                ClientApi clientApi = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);
                ApiResponse resp = clientApi.spider.scan(target, null, null, null, null);
                String scanId;
                int progress;

                scanId = ((ApiResponseElement) resp).getValue();
                while(true){
                    Thread.sleep(5000);
                    progress = Integer.parseInt(((ApiResponseElement) clientApi.spider.status(scanId)).getValue());
                    System.out.println("Progress: " + progress);
                    if(progress >=100){
                        break;
                    }
                }

            } catch (Exception e) {
                System.out.println("Exception : " + e.getMessage());
                e.printStackTrace();

            }
        }

        if(args[0].equals("attack")){
            try {
                ClientApi clientApi = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);
                ApiResponse resp = clientApi.ascan.scan(target, "true", "false", null, null, null);
                String scanId;
                int progress;

                scanId = ((ApiResponseElement) resp).getValue();
                while(true){
                    Thread.sleep(5000);
                    progress = Integer.parseInt(((ApiResponseElement) clientApi.ascan.status(scanId)).getValue());
                    System.out.println("Progress: " + progress);
                    if(progress >=100){
                        break;
                    }
                }

                byte[] report = clientApi.core.htmlreport();
                writeReport(report);
                
                stopZap(clientApi);

            } catch (Exception e) {

            }
        }


    }
}
