//HUDSTAGE
package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.List;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;

public class HudStage extends AbstractStage implements OnNextTurnListener {
    private final Color[] playerColors = new Color[]{Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED, Color.ORANGE};
    private String[] currentPlayerNames;
    private Color[] currentPlayerColors;
    private int[] occupiedTerritoriesCount;
    private int playersCount;
    private Color arrayT[] = new Color[41];
    private PhaseDisplay phaseDisplay;
    private OnHUDInteractionListener hudInteractionListener;
    private String yourTurn;

    //Labels
    private Label[] currentPlayerLabels;
    private Label unitsLabel;
    private Label statisticsOpponentsLabel;
    private Label[] occupiedTerritoriesLabel;
    private Label yourTurnLabel;


    public HudStage(AbstractScreen screen, Viewport vp, List<Player> currentPlayers, OnHUDInteractionListener l){
        super(vp, screen);
        currentPlayerNames = new String[currentPlayers.size()];
        currentPlayerColors = new Color[currentPlayers.size()];
        currentPlayerLabels = new Label[currentPlayers.size()];
        occupiedTerritoriesLabel = new Label[currentPlayers.size()];
        occupiedTerritoriesCount = new int[currentPlayers.size()];
        playersCount = currentPlayers.size();
        setCurrentPlayersColorOnHud(currentPlayers);

        //from temphud
        this.hudInteractionListener = l;
        setupPhaseDisplay();

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        unitsLabel = new Label("Statistik", new Label.LabelStyle(generateFont(), Color.WHITE));
        yourTurnLabel= new Label(yourTurn, new Label.LabelStyle(generateFont(), Color.valueOf("#ff0000ff")));
        statisticsOpponentsLabel = new Label("Spieler", new Label.LabelStyle(generateFont(), Color.WHITE));

        //row 1
        table.add(unitsLabel).width(vp.getScreenWidth()/3).padTop(5).padLeft(15);
        table.add(yourTurnLabel).width(vp.getScreenWidth()/3).expandX().padTop(5);
        table.add(statisticsOpponentsLabel).expandX().right().padTop(5).padRight(15);
        table.row();
        //remaining rows
        for(int i = 0; i < playersCount; i++){
            currentPlayerLabels[i] = new Label(currentPlayerNames[i], new Label.LabelStyle(generateFont(), Color.valueOf(currentPlayerColors[i].toString())));
            occupiedTerritoriesLabel[i] = new Label( "Territorien: " + occupiedTerritoriesCount[i] + " / 42", new Label.LabelStyle(generateFont(), Color.valueOf(currentPlayerColors[i].toString())));
            table.add(occupiedTerritoriesLabel[i]).width(vp.getScreenWidth()/3).padLeft(15);
            table.add().width(vp.getScreenWidth()/3);
            table.add(currentPlayerLabels[i]).expandX().right().padRight(15);
            table.row();
        }
        this.addActor(table);
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
            this.occupiedTerritoriesCount[i] = 0;
        }
    }

    public void update() {
        yourTurnLabel.setText(this.yourTurn);
        for(int i = 0; i < this.playersCount; i++){
            occupiedTerritoriesLabel[i].setText("Territorien: " + this.occupiedTerritoriesCount[i] + " / 42");
        }
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

    @Override
    public void isPlayersTurnNow(int playerID, boolean isThisPlayer) {
        this.setMessage(isThisPlayer);
    }

    private void setupPhaseDisplay() {
        this.phaseDisplay = new PhaseDisplay(getScreen().getGame().getAssetManager());
        this.addActor(phaseDisplay);
        phaseDisplay.setWidth(Gdx.graphics.getWidth());
        phaseDisplay.setHeight(Gdx.graphics.getHeight()/7f);
        phaseDisplay.setOrigin(Align.center);
    }

    public void setPhase(Database.Phase phase) {
        phaseDisplay.setPhase(phase);
        if ((phase == Database.Phase.ATTACKING || phase == Database.Phase.MOVING) &&
                Database.getInstance().isThisPlayersTurn()) {

            phaseDisplay.setSkipButtonVisible(true);
            phaseDisplay.setSkipButtonListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    hudInteractionListener.stageSkipButtonClicked();
                }
            });
        }
        else {
            phaseDisplay.setSkipButtonVisible(false);
        }
    }

    public void setPlayerTerritoryCount(int territoryID, int playerColor){
        this.arrayT[territoryID] = Territory.getByID(territoryID).getArmyColor();

        for(int i = 0; i < this.playersCount; i++){
            if(this.currentPlayerColors[i] == this.playerColors[playerColor]){
                this.occupiedTerritoriesCount[i] = 0;
            }
        }

       for (Color territoryColor : this.arrayT
             ) {
            if(territoryColor != null){
                if(territoryColor == this.playerColors[playerColor]){
                    for(int i = 0; i < this.playersCount; i++){
                        if(this.currentPlayerColors[i] == territoryColor){
                            this.occupiedTerritoriesCount[i] = this.occupiedTerritoriesCount[i] + 1;
                        }
                    }
                }
            }
        }
    }
}

