


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


import java.io.IOException;

import java.security.GeneralSecurityException;
import java.util.*;

import static java.lang.System.getProperties;


class Bot extends TelegramLongPollingBot{


    private String[] genreMas = new String[]{"Драма", "Комедия", "Мелодрама", "Романтика", "Боевик", "Семейный фильм", "Документальный","Фантастика", "Биография", "Военный", "Исторический"};
    private String[] themeMas = new String[]{"Война", "Политика", "Гендерная идентичность", "Болезни", "Семья", "Равенство полов", "Природа и Экология", "Назад"};
    Map<Long, User> usersStates = new HashMap<>();

    public static void main(String[] args) {
        getProperties().put("proxySet", "true");

        getProperties().put("socksProxyHost", "127.0.0.1");

        getProperties().put("socksProxyPort", "9150");
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
        return "WatchAndThinkBot";
        //возвращаем юзера
    }

    @Override
    public void onUpdateReceived(Update e) {
        Message message = e.getMessage();
        if(message != null && message.hasText()) {
            if(!usersStates.containsKey(message.getChatId())) {
                usersStates.put(message.getChatId(), new User());
            }
            User user = usersStates.get(message.getChatId());

            if(user.state.equals("START")) {
                  switch (message.getText().toLowerCase()){

                      case "выбрать фильм":

                          user.state = "THEME";
                          usersStates.put(message.getChatId(), user) ;
                          sendMsg(message,"Выберите Тему", themeMas);
                          break;
                      default:
                          sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                          break;
                  }
              } else{
              if(user.state.equals("THEME")) {          //если человек не записал тему

                  if(Arrays.asList(themeMas).contains(message.getText()) && !message.getText().toLowerCase().equals("назад")){       //если он выбрал тему то..
                      user.theme = message.getText().toLowerCase();// присваи(е)ваем теме значение
                      user.state = "GENRE";
                      usersStates.put(message.getChatId(), user) ;
                      try {
                          user.genres = genresForTheme(genreMas, user.theme);
                      } catch (GeneralSecurityException ex) {
                          ex.printStackTrace();
                      } catch (IOException ex) {
                          ex.printStackTrace();
                      }

                          sendMsg(message,"Выберите Жанр", user.genres);

                  }else {
                      switch (message.getText().toLowerCase()){
                          case "назад":
                          case "/start":

                              usersStates.put(message.getChatId(), new User()) ;
                              sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                              break;
                          default:
                              sendMsg(message,"Выберите Тему", themeMas);
                              break;
                      }
                  }

              }else {                                                                 //тут уже в теме что то лежит
                  if(user.state.equals("GENRE")) {
                      if(Arrays.asList(genreMas).contains(message.getText()) && !message.getText().toLowerCase().equals("назад")) {       // а если человек и жанр выбрал то выдаем ему фильм
                          user.genre = message.getText().toLowerCase();
                          user.state = "MORE";
                          usersStates.put(message.getChatId(), user) ;

                          try {
                              user.films = GoogleSheetsIntegration.selectedFilms(user.theme, user.genre);
                              ifNoFilms(message, user);
                          } catch (IOException ex) {
                              ex.printStackTrace();
                          } catch (GeneralSecurityException ex) {
                              ex.printStackTrace();
                          }


                      } else {// написал фигню

                          switch (message.getText().toLowerCase()) {
                              case "назад":
                                  user.theme = "";
                                  user.state = "THEME";
                                  usersStates.put(message.getChatId(), user) ;
                                  sendMsg(message, "Выберите Тему", themeMas);
                                  break;
                              case "/start":
                                  usersStates.put(message.getChatId(), new User()) ;

                                  sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                                  break;

                              default:

                                      sendMsg(message,"Выберите Жанр", user.genres);

                                  break;
                          }
                      }
                  } else {
                      switch (message.getText().toLowerCase()) {
                          case "/start":
                              usersStates.put(message.getChatId(), new User()) ;
                              sendMsg(message, "Здравсвуйте!\n\nХотите посмотреть фильм?\nНажмите 'Выбрать Фильм'\n", new String[]{"Выбрать Фильм"});
                              break;
                          case "еще фильм":
                              ifNoFilms(message, user);
                              break;
                          case "новый фильм":
                              user.films.removeAll(user.films);
                              user.state = "THEME";
                              user.theme = "";
                              user.genre = "";
                              usersStates.put(message.getChatId(), user) ;

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

private Film getRandomFilm(User user) {
    int a = 0; // Начальное значение диапазона - "от"
    int b = user.films.size(); // Конечное значение диапазона - "до"
    int randIndex = a + (int)(Math.random()*b);
    Film strFilm = user.films.get(randIndex);
    user.films.remove(randIndex);
        return strFilm;
}
private void ifNoFilms(Message message, User user) {
        if(user.films.isEmpty()) {
            sendMsg(message, "Извините, такие фильмы закончились", new String[]{"Новый Фильм"});
        } else {
            Film film = getRandomFilm(user);
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
        return "1177485989:AAEj5-dfK86RLGpspm8B1Q34fkQ7iXsCb9Y";
        //Токен бота
    }

}