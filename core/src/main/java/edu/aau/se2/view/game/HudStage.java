//HUDSTAGE
package edu.aau.se2.view.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;


import java.util.Locale;

import edu.aau.se2.RiskGame;


public class HudStage extends Stage implements Disposable {
    private Integer scoreOwn;
    private Integer scoreOpponentOne;
    private Integer scoreOpponentTwo;
    private Integer scoreOpponentThree;
    private Integer attacksMadeAmount;
    private Integer attacksMadeSucceededAmount;
    private Integer attacksGotAmount;
    private Integer attacksGotSucceededAmount;

    // stings
    private String nameOpponentOne;
    private String nameOpponentTwo;
    private String nameOpponentThree;

    //Labels
    private Label statisticsOwnLabel;
    private Label statisticsOpponentsLabel;
    private Label attacksMadeLabel;
    private Label attacksGotLabel;
    private Label scoreOwnLabel;
    private Label colorOwn;
    private Label scoreOpponentOneLabel;
    private Label scoreOpponentTwoLabel;
    private Label scoreOpponentThreeLabel;


    public HudStage(Viewport vp){
        //TODO: values from server
        super(vp);
        scoreOwn = 544;
        scoreOpponentOne = 412;
        scoreOpponentTwo = 568;
        scoreOpponentThree = 651;

        attacksMadeAmount = 5;
        attacksMadeSucceededAmount = 3;

        attacksGotAmount = 2;
        attacksGotSucceededAmount = 2;

        nameOpponentOne = "Player 1";
        nameOpponentTwo = "Player 2";
        nameOpponentThree = "Player 3";


        //this viewport will be most likely different from the viewport od the risk-world-map-viewport, since not zoomable
        //viewport = new FitViewport(RiskGame.V_WIDTH, RiskGame.V_HEIGHT, new OrthographicCamera());
        //create the hudStage with the hud-spec-viewport
        //stage = new Stage(viewport, spriteBatch);

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        //Own data
        statisticsOwnLabel = new Label("Statistics", new Label.LabelStyle(new BitmapFont(), Color.GOLD));
        scoreOwnLabel = new Label("Score: " + String.format(Locale.US,"%4d", scoreOwn), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        attacksMadeLabel= new Label("Attacks made: " +  String.format(Locale.US,"%2d", attacksMadeAmount) + " / " +  String.format(Locale.US,"%2d", attacksMadeSucceededAmount), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        attacksGotLabel= new Label("Attacks got: " +  String.format(Locale.US,"%2d", attacksGotAmount) + " / " + String.format(Locale.US,"%2d", attacksGotSucceededAmount), new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        //Opponent data
        statisticsOpponentsLabel = new Label("Opponents", new Label.LabelStyle(new BitmapFont(), Color.GOLD));
        scoreOpponentOneLabel = new Label(nameOpponentOne + " : " + String.format(Locale.US, "%4d", scoreOpponentOne), new Label.LabelStyle(new BitmapFont(), Color.RED));
        scoreOpponentTwoLabel = new Label(nameOpponentTwo + " : " +  String.format(Locale.US,"%4d", scoreOpponentTwo), new Label.LabelStyle(new BitmapFont(), Color.GREEN));
        scoreOpponentThreeLabel = new Label(nameOpponentThree + " : " + String.format(Locale.US, "%4d", scoreOpponentThree), new Label.LabelStyle(new BitmapFont(), Color.BLUE));

        //row 1
        table.add(statisticsOwnLabel).expandX().padTop(5);
        table.add().expandX().padTop(5);
        table.add(statisticsOpponentsLabel).expandX().padTop(5);
        table.row();
        //row2
        table.add(scoreOwnLabel).expandX();
        table.add().expandX();
        table.add(scoreOpponentOneLabel).expandX();
        table.row();
        //row 3
        table.add(attacksMadeLabel).expandX();
        table.add().expandX();
        table.add(scoreOpponentTwoLabel).expandX();
        table.row();
        //row 4
        table.add().expandX();
        table.add().expandX();
        table.add(scoreOpponentThreeLabel).expandX();


        this.addActor(table);
    }

    @Override
    public void dispose() { this.dispose(); }
}

