package com.Bot;

import com.Bot.domain.Film;
import com.Bot.domain.User;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

    class GoogleSheetsIntegration {
        private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
        private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        private static final String TOKENS_DIRECTORY_PATH = "tokens";

        /**
         * Global instance of the scopes required by this quickstart.
         * If modifying these scopes, delete your previously saved tokens/ folder.
         */
        private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
        private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

        /**
         * Creates an authorized Credential object.
         *
         * @param HTTP_TRANSPORT The network HTTP Transport.
         * @return An authorized Credential object.
         * @throws IOException If the credentials.json file cannot be found.
         */
        private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
            // Load client secrets.
            InputStream in = GoogleSheetsIntegration.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }

        /**
         * Prints the names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         */
        public static ArrayList<Film> selectedFilms(String theme, String genre, User user) throws GeneralSecurityException, IOException {
            ArrayList<Film> films = new ArrayList<>();
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            final String spreadsheetId = "1kYWop6o-i_Sl_K60Ct6FLHKB6nLK6-nIWm0vcoOj8vw";
            final String range = "A2:F70";
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found.");
            } else {


                String durTheme = "";
                if (genre.equals("драма")) {
                    for(List row : values) {
                        if(!row.get(0).toString().isEmpty()) {
                            durTheme = row.get(0).toString().toLowerCase();
                        }
                        if(row.size() == 6) {
                            if(durTheme.equals(theme) && row.get(3).toString().replaceAll("мелодрама", "").contains(genre)) {
                                films.add(new Film(row.get(1).toString(), row.get(2).toString(), row.get(3).toString(), row.get(5).toString(), user));
                            }
                        }
                    }
                }else {


                    for (List row : values) {     // это ты берешь по одной строке

                        if (!row.get(0).toString().isEmpty()) {
                            durTheme = row.get(0).toString().toLowerCase();
                        }
                        if (row.size() == 6) {


                            if (durTheme.equals(theme) && row.get(3).toString().contains(genre)) {

                                films.add(new Film(row.get(1).toString(), row.get(2).toString(), row.get(3).toString(), row.get(5).toString(), user));
                            }
                        }


                    }

                }
                }
            return films;
            }
        }
