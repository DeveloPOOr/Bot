package com.Bot;


import com.Bot.domain.Film;
import com.Bot.domain.Genre;
import com.Bot.domain.User;
import com.Bot.services.UserService;
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




class Bot extends TelegramLongPollingBot{


    private String[] genreMas = new String[]{"Драма", "Комедия", "Мелодрама", "Романтика", "Боевик", "Семейный фильм", "Документальный","Фантастика", "Биография", "Военный", "Исторический"};
    private String[] themeMas = new String[]{"Война", "Политика", "Гендерная идентичность", "Болезни", "Семья", "Равенство полов", "Природа и Экология", "Назад"};
    UserService userService = new UserService();
    String hello = "Привет, друг \uD83E\uDD16\n" +
            "Это бот WatchAndThink!\n" +
            "Здесь ты сможешь найти фильм на любой вкус, просто нажми кнопку \n«Выбрать фильм»\uD83D\uDCA1";
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
        return "WatchAndThinkBot";
        //возвращаем юзера
    }

    @Override
    public void onUpdateReceived(Update e) {
        Message message = e.getMessage();
        if(message != null && message.hasText()) {
            if(userService.findUser(message.getChatId()) == null) {
                User user = new User();
                user.setState("START");
                user.setId(message.getChatId());
                userService.saveUser(user);
            }
            User user = userService.findUser(message.getChatId());


            if(user.getState().equals("START")) {

                  switch (message.getText().toLowerCase()){
                      case "выбрать фильм":
                          user.setState("THEME");

                          userService.updateUser(user);

                          sendMsg(message,"Выберите Тему", themeMas);
                          break;
                      default:
                          sendMsg(message, hello, new String[]{"Выбрать Фильм"});
                          break;
                  }
              } else{

              if(user.getState().equals("THEME")) {          //если человек не записал тему

                  if(Arrays.asList(themeMas).contains(message.getText()) && !message.getText().toLowerCase().equals("назад")){       //если он выбрал тему то..
                      user.setTheme(message.getText().toLowerCase());// присваи(е)ваем теме значение
                      user.setState("GENRE");

                      try {
                          user.setGenres(genresForTheme(genreMas, user.getTheme(), user));
                      } catch (GeneralSecurityException | IOException ex) {
                          ex.printStackTrace();
                      }

                      userService.updateUser(user);
                      sendMsg(message,"Выберите Жанр", user.getGenres());

                  }else {
                      switch (message.getText().toLowerCase()){
                          case "назад":
                          case "/start":

                              userService.deleteUser(user);
                              sendMsg(message, hello, new String[]{"Выбрать Фильм"});
                              break;
                          default:
                              sendMsg(message,"Выберите Тему", themeMas);
                              break;
                      }
                  }

              }else {                                                                 //тут уже в теме что то лежит
                  if(user.getState().equals("GENRE")) {
                      if(Arrays.asList(genreMas).contains(message.getText()) && !message.getText().toLowerCase().equals("назад")) {       // а если человек и жанр выбрал то выдаем ему фильм
                          user.setGenre(message.getText().toLowerCase());
                          user.setState("MORE");


                          try {
                              user.setFilms(GoogleSheetsIntegration.selectedFilms(user.getTheme(), user.getGenre(), user));
                              ifNoFilms(message, user);
                          } catch (IOException ex) {
                              ex.printStackTrace();
                          } catch (GeneralSecurityException ex) {
                              ex.printStackTrace();
                          }
                          userService.updateUser(user);

                      } else {// написал фигню

                          switch (message.getText().toLowerCase()) {
                              case "назад":
                                  user.setTheme("");
                                  user.setState("THEME");
                                  userService.saveUser(user);
                                  sendMsg(message, "Выберите Тему", themeMas);
                                  break;
                              case "/start":
                                  userService.deleteUser(user);

                                  sendMsg(message, hello, new String[]{"Выбрать Фильм"});
                                  break;

                              default:

                                      sendMsg(message,"Выберите Жанр", user.getGenres());

                                  break;
                          }
                      }
                  } else {
                      switch (message.getText().toLowerCase()) {
                          case "/start":
                              userService.deleteUser(user);
                              sendMsg(message, hello, new String[]{"Выбрать Фильм"});
                              break;
                          case "еще фильм":
                              ifNoFilms(message, user);
                              break;
                          case "новый фильм":

                              user.setFilms(new ArrayList<>());
                              user.setState("THEME");
                              user.setTheme("");
                              user.setGenre("");
                              user.setGenres(new LinkedHashSet<>());
                              userService.updateUser(user);

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

    public void sendMsg(Message message, String s, Set<Genre> buttons) {
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

    private void setButtons(SendMessage sendMessage, Set<Genre> buttons) {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        replyKeyboardMarkup.setOneTimeKeyboard(false); //не пропадет

        for(Genre button : buttons){
            KeyboardRow first = new KeyboardRow();
            first.add(button.getGenre());
            keyboardRowList.add(first);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

private Film getRandomFilm(User user) {
    int a = 0; // Начальное значение диапазона - "от"
    int b = user.getFilms().size(); // Конечное значение диапазона - "до"
    int randIndex = a + (int)(Math.random()*b);
    Film strFilm = user.getFilms().get(randIndex);
    user.getFilms().remove(randIndex); ///вот тут мэйби трабл
    userService.updateUser(user);
        return strFilm;
}
private void ifNoFilms(Message message, User user) {
        if(user.getFilms().isEmpty()) {
            sendMsg(message, "Извините, такие фильмы закончились", new String[]{"Новый Фильм"});
        } else {
            Film film = getRandomFilm(user);
            sendPhoto(message, film.getUrl(), film.filmToMsg());
            sendMsg(message, "Желаете посмотреть еще фильм из этой категории?\n\n Или хотите выбрать новый фильм?", new String[]{"Еще Фильм", "Новый Фильм"});
        }
}

private Set<Genre> genresForTheme(String[] allGenres, String theme, User user) throws GeneralSecurityException, IOException {
        Set<Genre> genres = new LinkedHashSet<>();
        StringBuilder check = new StringBuilder();
        List<Film> films = GoogleSheetsIntegration.selectedFilms(theme, "", null);
        for( Film film : films) {
            for(String genre : allGenres) {
                if(film.getGenre().contains(genre.toLowerCase()) && !check.toString().contains(genre)) {
                    check.append(genre);
                    Genre genre1 = new Genre();
                    genre1.setGenre(genre);
                    genre1.setUser(user);
                    genres.add(genre1);
                }
            }
        }
        Genre genre2 = new Genre();
        genre2.setGenre("Назад");
        genre2.setUser(user);
        genres.add(genre2);
        return genres;
}

    @Override
    public String getBotToken() {
        return "1177485989:AAEj5-dfK86RLGpspm8B1Q34fkQ7iXsCb9Y";
        //Токен бота
    }

}