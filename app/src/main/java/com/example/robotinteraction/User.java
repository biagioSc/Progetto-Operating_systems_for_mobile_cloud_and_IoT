package com.example.robotinteraction;

public class User {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String favouriteDrink; //usato per segnare le preferenze nel qustionario per i drink
    private String favouriteTopics; //usato per segnare le preferenze nel questionario per gli argomenti
                                    //su cui conversare




    public User(String name, String surname, String email, String password, String favouriteDrink, String favouriteTopics){
        setName(name);
        setSurname(surname);
        setEmail(email);
        setPassword(password);
        setFavouriteDrink(favouriteDrink);
        setFavouriteTopics(favouriteTopics);

    }

    public void setName(String name)
    {
        this.name=name;
    }

    public void setSurname(String surname)
    {
        this.surname=surname;
    }

    public void setEmail(String email)
    {
        this.email=email;
    }

    public void setPassword(String password)
    {
        this.password=password;
    }

    public void setFavouriteDrink(String drink)
    {
        this.favouriteDrink=drink;
    }

    public void setFavouriteTopics(String topics)
    {
        this.favouriteTopics=topics;
    }

    public String getName()
    {
        return this.name;
    }

    public String getSurname()
    {
        return this.surname;
    }

    public String getEmail()
    {
        return this.email;
    }

    public String getPassword()
    {
        return this.password;
    }

    public String getFavouriteDrink()
    {
        return this.favouriteDrink;
    }

    public String getFavouriteTopics()
    {
        return this.favouriteTopics;
    }

}
