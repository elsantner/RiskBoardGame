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
import java.util.Locale;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;

public class HudStage extends AbstractStage implements OnNextTurnListener {
    private final Color[] playerColors = new Color[]{Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED, Color.ORANGE};
    private String[] currentPlayerNames;
    private Color[] currentPlayerColors;
    private Label[] currentPlayerLabels;
    private int occupiedTerritoriesCount = 0;
    private int playersCount;
    private Color arrayT[] = new Color[41];

    //from temphudstage
    private PhaseDisplay phaseDisplay;
    private OnHUDInteractionListener hudInteractionListener;

    private Integer attacksMadeAmount;
    private Integer attacksMadeSucceededAmount;
    private Integer attacksGotAmount;
    private Integer attacksGotSucceededAmount;

    //Labels
    private Label unitsLabel;
    private Label statisticsOpponentsLabel;
    private Label occupiedTerritoriesLabel;
    private Label attacksGotLabel;
    private String yourTurn;
    private Label yourTurnLabel;


    public HudStage(AbstractScreen screen, Viewport vp, List<Player> currentPlayers, OnHUDInteractionListener l){
        //TODO: values from server
        super(vp, screen);
        currentPlayerNames = new String[currentPlayers.size()];
        currentPlayerColors = new Color[currentPlayers.size()];
        currentPlayerLabels = new Label[currentPlayers.size()];
        playersCount = currentPlayers.size();
        setCurrentPlayersColorOnHud(currentPlayers);

        //from temphud
        this.hudInteractionListener = l;
        setupPhaseDisplay();

        attacksMadeAmount = 5;
        attacksMadeSucceededAmount = 3;

        attacksGotAmount = 2;
        attacksGotSucceededAmount = 2;

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        unitsLabel = new Label("Statistik", new Label.LabelStyle(generateFont(), Color.WHITE));
        //occupiedTerritoriesLabel= new Label("Territorien erobert: " + String.format(Locale.US, "%2", occupiedTerritoriesCount) + " / 42" , new Label.LabelStyle(generateFont(), Color.WHITE));
        occupiedTerritoriesLabel = new Label( "Territorien 42 / " + occupiedTerritoriesCount , new Label.LabelStyle(generateFont(), Color.WHITE));
        yourTurnLabel= new Label(yourTurn, new Label.LabelStyle(generateFont(), Color.valueOf("#ff0000ff")));
        statisticsOpponentsLabel = new Label("Spieler", new Label.LabelStyle(generateFont(), Color.WHITE));

        //row 1
        table.add(unitsLabel).expandX().padTop(5);
        table.add(yourTurnLabel).expandX().padTop(5);
        table.add(statisticsOpponentsLabel).expandX().padTop(5);
        table.row();
        table.add(occupiedTerritoriesLabel);
        table.row();
        //remaining rows
        for(int i = 0; i < playersCount; i++){
            currentPlayerLabels[i] = new Label(currentPlayerNames[i], new Label.LabelStyle(generateFont(), Color.valueOf(currentPlayerColors[i].toString())));
            table.add().expandX();
            table.add().expandX();
            table.add(currentPlayerLabels[i]).expandX();
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
        }
    }

    private Color[] getPlayerColors(List<Player> currentPlayers){
        Color[] playerColors = new Color[currentPlayers.size()];
        for(int i = 0; i < currentPlayers.size(); i++){
            playerColors[i] = this.playerColors[currentPlayers.get(i).getColorID()];
        }
        return playerColors;
    }

    public void update() {
        yourTurnLabel.setText(this.yourTurn);
        occupiedTerritoriesLabel.setText("Territorien 42 / " + this.occupiedTerritoriesCount);
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
        Color currentColor = this.playerColors[playerColor];
        //System.out.println("### this playersColors: " + this.playerColors[0]);
        Territory.getByID(territoryID).getArmyColor();
        Territory.getByID(territoryID).getTerritoryName();
        //System.out.println("###id: " + territoryID + " : " + Territory.getByID(territoryID).getTerritoryName() + Territory.getByID(territoryID).getArmyColor());
        this.arrayT[territoryID] = Territory.getByID(territoryID).getArmyColor();
        this.occupiedTerritoriesCount = 0;
        for (Color territory : this.arrayT
             ) {
            if(territory != null){
                System.out.println("### terr in the array: " + territory.toString());
                System.out.println("### currentColory: " + currentColor.toString());
                if(territory == currentColor){
                    occupiedTerritoriesCount = occupiedTerritoriesCount+1;
                }
                System.out.println("### occupiedTerritories" + occupiedTerritoriesCount);
            }
        }
    }
}

