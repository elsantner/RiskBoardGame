//HUDSTAGE
package edu.aau.se2.view.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.List;


import java.util.Locale;

import edu.aau.se2.model.Database;
import edu.aau.se2.server.data.Player;

public class HudStage extends Stage implements IGameBoard {
    private boolean armiesPlacable = false;
    private Integer thisPlayerColorId;
    private int[] thisPlayerColorIdArray;
    private Color[] playerColors;

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
    private Label scoreOpponentOneLabel;
    private Label scoreOpponentTwoLabel;
    private Label scoreOpponentThreeLabel;
    private String yourTurn;
    private Label yourTurnLabel;


    public HudStage(Viewport vp){
        //TODO: values from server
        super(vp);
        playerColors = new Color[]{Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED, Color.ORANGE};

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
        yourTurnLabel= new Label(yourTurn, new Label.LabelStyle(new BitmapFont(), Color.valueOf("#ff0000ff")));

        //Opponent data
        statisticsOpponentsLabel = new Label("Opponents", new Label.LabelStyle(new BitmapFont(), Color.GOLD));
        scoreOpponentOneLabel = new Label(nameOpponentOne + " : " + String.format(Locale.US, "%4d", scoreOpponentOne), new Label.LabelStyle(new BitmapFont(), Color.RED));
        scoreOpponentTwoLabel = new Label(nameOpponentTwo + " : " +  String.format(Locale.US,"%4d", scoreOpponentTwo), new Label.LabelStyle(new BitmapFont(), Color.GREEN));
        scoreOpponentThreeLabel = new Label(nameOpponentThree + " : " + String.format(Locale.US, "%4d", scoreOpponentThree), new Label.LabelStyle(new BitmapFont(), Color.BLUE));

        //row 1
        table.add(statisticsOwnLabel).expandX().padTop(5);
        table.add(yourTurnLabel).expandX().padTop(5);
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
        table.add(attacksGotLabel).expandX();
        table.add().expandX();
        table.add(scoreOpponentThreeLabel).expandX();

        System.out.println("##### thisPlayerColorId " +  thisPlayerColorId);
        this.addActor(table);
    }

    @Override
    public void dispose() { super.dispose(); }


    @Override
    public void setInteractable(boolean interactable) {
    }

    @Override
    public boolean isInteractable() {
        return true;
    }

    @Override
    public boolean isArmiesPlacable() {
        return true;
    }

    @Override
    public void setAttackAllowed(boolean attackAllowed) {
    }

    @Override
    public boolean isAttackAllowed() {
        return true;
    }

    @Override
    public void setArmyCount(int territoryID, int count) {
    }

    @Override
    public void setArmyColor(int territoryID, int colorID) {
    }

    @Override
    public void setArmiesPlacable(boolean armiesPlacable) {
        this.armiesPlacable = armiesPlacable;
        this.setMessage(armiesPlacable);
    }

    private void setMessage(boolean isPlayersTurn) {
        if(isPlayersTurn){
            this.yourTurn = "Your turn";
        } else {
            this.yourTurn = "";
        }
    }

    public void setCurrentPlayersColorOnHud(List<Player> currentPlayers){
        for (Player user : currentPlayers) {
            this.setColor(currentPlayers, user.getColorID(), user.getNickname());
        }
    }

    private void setColor(List<Player> currentPlayers, int colorID, String nickName) {

        //System.out.println("####BIG HOPE" + colorID);
        //System.out.println("###" + playerColors[colorID]);
        //System.out.println("###currentPlayers.size() : " + currentPlayers.size());
        for(int i = 0; i < currentPlayers.size(); i++){
            System.out.println("###USER " + i + " " + this.playerColors[colorID] + " " + nickName);
        }
    }

    public void update() {
        yourTurnLabel.setText(this.yourTurn);
    }
}

