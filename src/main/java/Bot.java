


import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.getProperties;

class Bot extends TelegramLongPollingBot{
        private String nice = "\n\nПриятного просмотра!";
    public String theme = "";
    public  String genre = "";
    private String[] genreMas = new String[]{"Драма", "Комедия", "Мелодрама", "Романтика", "Боевик", "Семейный фильм", "Документальный","Фантастика", "Биография", "Военный", "Исторический"};
    private String[] themeMas = new String[]{"Война", "Политика", "Гендерная идентичность", "Болезни", "Семья", "Равенство полов", "Природа и Экология", "Назад"};
    private boolean button = false;
    private ArrayList<Film> films = new ArrayList<>();
    String[] genres = new String[0];
    public static void main(String[] args) {

        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            botapi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return "film_123bot";
        //возвращаем юзера
    }

    @Override
    public void onUpdateReceived(Update e) {
        Message message = e.getMessage();
        if(message != null && message.hasText()) {



            if(!button) {
                  switch (message.getText()){

                      case "Выбрать Фильм":
                          button = true;
                          sendMsg(message,"Выберите Тему", themeMas);
                          break;
                      default:
                          sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                          break;
                  }
              } else{
              if(theme.isEmpty()) {          //если человек не записал тему
                  if(Arrays.asList(themeMas).contains(message.getText()) && !message.getText().equals("Назад")){       //если он выбрал тему то..
                      theme = message.getText().toLowerCase();  // присваи(е)ваем теме значение

                      try {
                          genres = genresForTheme(genreMas, this.theme);
                      } catch (GeneralSecurityException ex) {
                          ex.printStackTrace();
                      } catch (IOException ex) {
                          ex.printStackTrace();
                      }

                          sendMsg(message,"Выберите Жанр", genres);

                  }else {
                      switch (message.getText()){
                          case "Назад":
                          case "/start":
                              button = false;
                              sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                              break;
                          default:
                              sendMsg(message,"Выберите Тему", themeMas);
                              break;
                      }
                  }

              }else {                                                                 //тут уже в теме что то лежит
                  if(genre.isEmpty()) {
                      if(Arrays.asList(genreMas).contains(message.getText()) && !message.getText().equals("Назад")) {       // а если человек и жанр выбрал то выдаем ему фильм
                          genre = message.getText().toLowerCase();


                          try {
                              films = GoogleSheetsIntegration.selectedFilms(theme, genre);
                              ifNoFilms(message);
                          } catch (IOException ex) {
                              ex.printStackTrace();
                          } catch (GeneralSecurityException ex) {
                              ex.printStackTrace();
                          }


                      } else {// написал фигню

                          switch (message.getText()) {
                              case "Назад":
                                  theme = "";
                                  sendMsg(message, "Выберите Тему", themeMas);
                                  break;
                              case "/start":
                                  button = false;
                                  theme = "";
                                  sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                                  break;

                              default:

                                      sendMsg(message,"Выберите Жанр", genres);

                                  break;
                          }
                      }
                  } else {
                      switch (message.getText()) {
                          case "/start":
                              button = false;
                              theme = "";
                              genre = "";
                              sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                              break;
                          case "Еще Фильм":
                              ifNoFilms(message);
                              break;
                          case "Новый Фильм":
                              films.removeAll(films);
                              theme = "";
                              genre = "";
                              sendMsg(message, "Выберите Тему", themeMas);
                              break;
                          default:

                              sendMsg(message, "Желаете посмотреть еще фильм из этой категории?\n\n Или хотите выбрать новый фильм?", new String[]{"Еще Фильм", "Новый Фильм"});
                              break;

                      }
                  }
              }
              }
        }
    }


    private void sendMsg(Message message, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());

        sendMessage.setText(s);
        try{

            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String s, String[] buttons) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());

        sendMessage.setText(s);
        try{
            setButtons(sendMessage, buttons);
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(Message message, String url, String description) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId());

        sendPhoto.setPhoto(url);
        sendPhoto.setCaption(description);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void setButtons(SendMessage sendMessage, String[] buttons) {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        replyKeyboardMarkup.setOneTimeKeyboard(false); //не пропадет

        for(String button : buttons){
            KeyboardRow first = new KeyboardRow();
            first.add(button);
            keyboardRowList.add(first);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

private Film getRandomFilm() {
    int a = 0; // Начальное значение диапазона - "от"
    int b = films.size(); // Конечное значение диапазона - "до"
    int randIndex = a + (int)(Math.random()*b);
    Film strFilm = films.get(randIndex);
    films.remove(randIndex);
        return strFilm;
}
private void ifNoFilms(Message message) {
        if(films.isEmpty()) {
            sendMsg(message, "Извините такие фильмы закончились", new String[]{"Новый Фильм"});
        } else {
            Film film = getRandomFilm();
            sendPhoto(message, film.getUrl(), film.filmToMsg());
            sendMsg(message, "Желаете посмотреть еще фильм из этой категории?\n\n Или хотите выбрать новый фильм?", new String[]{"Еще Фильм", "Новый Фильм"});
        }
}

private String[] genresForTheme(String[] allGenres, String theme) throws GeneralSecurityException, IOException {
        StringBuilder ans = new StringBuilder();
        ArrayList<Film> films = GoogleSheetsIntegration.selectedFilms(theme, "");
        for( Film film : films) {
            for(String genre : allGenres) {
                if(film.getGenre().contains(genre.toLowerCase()) && !ans.toString().contains(genre)) {
                    ans.append(genre);
                    ans.append(" ");
                }
            }
        }
        ans.append("Назад");
        return ans.toString().split(" ");
}

    @Override
    public String getBotToken() {
        return "1282277506:AAEsm5gquEqshTPToIzgI3dFHsAlTRC9maU";
        //Токен бота
    }

}