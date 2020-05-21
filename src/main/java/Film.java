public class Film {
    private String name = "";
    private String description = "";
    private String genre = "";
    private String url = "";
    public Film(String name, String description, String genre, String url) {
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.url = url;
    }
    public String filmToMsg() {
        StringBuilder ans = new StringBuilder();
        ans.append(name);
        ans.append("\n\n");
        ans.append(genre);
        ans.append("\n\nОписание:\n");
        ans.append(description);
        return ans.toString();
    }
    public String getUrl() {
        return this.url;
    }
    public String getGenre() {return  this.genre;}

}
