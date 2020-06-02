package edu.aau.se2.model.listener;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.DefaultNameProvider;

public class TextInputListener implements Input.TextInputListener {
    private String textInput;
    private DefaultNameProvider defaultNameProvider;

    public TextInputListener() {
    }

    @Override
    public void input(String userInput) {
        textInput = userInput;
        System.out.println("###input+ " + textInput);
        //
    }

    @Override
    public void canceled() {
        textInput = "Default";
    }

    public String getTextInput(){ return textInput; }

    public void setTextInput(String text){
        textInput = text;
    }

}
