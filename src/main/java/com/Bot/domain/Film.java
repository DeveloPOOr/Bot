package com.Bot.domain;

import javax.persistence.*;

@Entity
@Table
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name ;
    @Column(length = 5000)
    private String description;
    private String genre;
    @Column(length = 5000)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    private com.Bot.domain.User user;

    public Film(){}

    public Film(String name, String description, String genre, String url, User user) {
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.url = url;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setUrl(String url) {
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

    public String getGenre() {
        return genre;
    }

    public String getUrl() {
        return url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
