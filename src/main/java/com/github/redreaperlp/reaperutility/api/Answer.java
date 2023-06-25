package com.github.redreaperlp.reaperutility.api;

import com.github.redreaperlp.reaperutility.util.Color;
import org.json.JSONArray;
import org.json.JSONObject;

public class Answer {

    JSONObject answer = new JSONObject();

    public Answer() {
    }

    public Answer(String answer) {
        this.answer.put("answer", answer);
    }

    public Answer(String answer, int status) {
        this.answer.put("answer", answer);
        this.answer.put("status", status);
    }

    public Answer(int status, JSONObject answer) {
        this.answer.put("status", status);
        this.answer.put("answer", answer);
    }

    public JSONArray answerArray() {
        return answer.getJSONArray("answer");
    }

    public Answer set(String answer) {
        this.answer.put("answer", answer);
        return this;
    }

    public Answer set(JSONObject answer) {
        this.answer.put("answer", answer);
        return this;
    }

    public Answer set(JSONArray answer) {
        this.answer.put("answer", answer);
        return this;
    }

    public Answer status(int status) {
        this.answer.put("status", status);
        return this;
    }

    public int status() {
        return answer.getInt("status");
    }

    @Override
    public String toString() {
        System.out.println("Answer: ");
        Color.printTest(answer.toString(), Color.GREEN);
        return answer.toString();
    }
}
