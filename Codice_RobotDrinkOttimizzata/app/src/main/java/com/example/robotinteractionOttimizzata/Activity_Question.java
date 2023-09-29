package com.example.robotinteractionOttimizzata;

public class Activity_Question {
    private String topic;
    private String question;
    private String correctAnswer;
    private String wrongAnswer1;
    private String wrongAnswer2;
    private String wrongAnswer3;


    public Activity_Question(String topic, String question, String correctAnswer, String wrongAnswer1, String wrongAnswer2, String wrongAnswer3) {
        this.topic = topic;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.wrongAnswer1 = wrongAnswer1;
        this.wrongAnswer2 = wrongAnswer2;
        this.wrongAnswer3 = wrongAnswer3;
    }

    public String getTopic() {
        return topic;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getWrongAnswer1() {
        return wrongAnswer1;
    }

    public String getWrongAnswer2() {
        return wrongAnswer2;
    }
    public String getWrongAnswer3() {
        return wrongAnswer3;
    }

}
