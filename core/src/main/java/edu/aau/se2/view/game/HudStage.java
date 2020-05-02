//HUDSTAGE
package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.List;
import java.util.Locale;
import edu.aau.se2.server.data.Player;

public class HudStage extends Stage implements IGameBoard {
    private boolean armiesPlacable = false;
    private Integer thisPlayerColorId;
    private int[] thisPlayerColorIdArray;
    private Color[] playerColors;
    private String[] currentPlayerNames;
    private Color[] currentPlayerColors;
    private Label[] currentPlayerLabels;
    private BitmapFont niceFont;
    private int[] currentArmyReserveCount;
    private int playersCount;

    private Integer attacksMadeAmount;
    private Integer attacksMadeSucceededAmount;
    private Integer attacksGotAmount;
    private Integer attacksGotSucceededAmount;

    //Labels
    private Label unitsLabel;
    private Label statisticsOpponentsLabel;
    private Label attacksMadeLabel;
    private Label attacksGotLabel;
    private Label scoreOwnLabel;
    private String yourTurn;
    private Label yourTurnLabel;


    public HudStage(Viewport vp, List<Player> currentPlayers){
        //TODO: values from server
        super(vp);
        playerColors = new Color[]{Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED, Color.ORANGE};
        currentPlayerNames = new String[currentPlayers.size()];
        currentPlayerColors = new Color[currentPlayers.size()];
        currentPlayerLabels = new Label[currentPlayers.size()];
        currentArmyReserveCount = new int[currentPlayers.size()];
        playersCount = currentPlayers.size();
        setCurrentPlayersColorOnHud(currentPlayers);

        attacksMadeAmount = 5;
        attacksMadeSucceededAmount = 3;

        attacksGotAmount = 2;
        attacksGotSucceededAmount = 2;

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        unitsLabel = new Label("Einheiten", new Label.LabelStyle(generateFont(), Color.WHITE));
        attacksMadeLabel= new Label("Attacks made: " +  String.format(Locale.US,"%2d", attacksMadeAmount) + " / " +  String.format(Locale.US,"%2d", attacksMadeSucceededAmount), new Label.LabelStyle(generateFont(), Color.WHITE));
        attacksGotLabel= new Label("Attacks got: " +  String.format(Locale.US,"%2d", attacksGotAmount) + " / " + String.format(Locale.US,"%2d", attacksGotSucceededAmount), new Label.LabelStyle(generateFont(), Color.WHITE));
        yourTurnLabel= new Label(yourTurn, new Label.LabelStyle(generateFont(), Color.valueOf("#ff0000ff")));
        statisticsOpponentsLabel = new Label("Spieler", new Label.LabelStyle(generateFont(), Color.WHITE));

        //row 1
        table.add(unitsLabel).expandX().padTop(5);
        table.add(yourTurnLabel).expandX().padTop(5);
        table.add(statisticsOpponentsLabel).expandX().padTop(5);
        table.row();
        //remaining rows
        for(int i = 0; i < currentPlayers.size(); i++){
            currentPlayerLabels[i] = new Label(currentPlayerNames[i], new Label.LabelStyle(generateFont(), Color.valueOf(currentPlayerColors[i].toString())));
            table.add().expandX();
            table.add().expandX();
            table.add(currentPlayerLabels[i]).expandX();
            table.row();
        }
        this.addActor(table);
    }

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
            this.yourTurn = "Deine Runde";
        } else {
            this.yourTurn = "";
        }
    }

    public void setCurrentPlayersColorOnHud(List<Player> currentPlayers){
        for(int i = 0; i < currentPlayers.size(); i++){
            this.currentPlayerNames[i] = currentPlayers.get(i).getNickname();
            this.currentPlayerColors[i] = this.playerColors[currentPlayers.get(i).getColorID()];
            //remaining units are going to be set here
            this.currentArmyReserveCount[i] = currentPlayers.get(i).getArmyReserveCount();
        }
    }

    public void update() {
        yourTurnLabel.setText(this.yourTurn);
        /*for(int i = 0; i < this.playersCount; i++){
            currentPlayerLabels[i].setText(currentPlayerNames[i]);
            System.out.println("####" + currentPlayerLabels[i]);
        }*/
    }

    private BitmapFont generateFont(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/CenturyGothic.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 32;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    @Override
    public void dispose() { super.dispose(); }

}

