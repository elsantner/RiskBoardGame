//HUDSTAGE
package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.List;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;

public class HudStage extends AbstractStage implements OnNextTurnListener {
    private final Color[] playerColors = new Color[]{Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED, Color.ORANGE};
    private String[] currentPlayerNames;
    private Color[] currentPlayerColors;
    private int[] occupiedTerritoriesCount;
    private int playersCount;
    private Color[] arrayT = new Color[42];
    private PhaseDisplay phaseDisplay;
    private AttackDisplay attackDisplay;
    private OnHUDInteractionListener hudInteractionListener;
    private String yourTurn;
    private boolean showCards;
    private Database db;

    //Labels
    private Label[] currentPlayerLabels;
    private Label[] occupiedTerritoriesLabel;
    private Label yourTurnLabel;

    public HudStage(AbstractScreen screen, Viewport vp, List<Player> currentPlayers, OnHUDInteractionListener l){
        super(vp, screen);
        db = Database.getInstance();
        currentPlayerNames = new String[currentPlayers.size()];
        currentPlayerColors = new Color[currentPlayers.size()];
        currentPlayerLabels = new Label[currentPlayers.size()];
        occupiedTerritoriesLabel = new Label[currentPlayers.size()];
        occupiedTerritoriesCount = new int[currentPlayers.size()];
        playersCount = currentPlayers.size();
        setCurrentPlayersColorOnHud(currentPlayers);
        showCards = false;

        setupPhaseDisplay();
        setupHUD();
        this.hudInteractionListener = l;
        setupAttackDisplay();
        attackDisplay.setVisible(true);
    }

    private void setupHUD() {
        Viewport vp = getViewport();

        TextButton cards = new TextButton("Spielkarten", (Skin) getScreen().getGame().getAssetManager().get(AssetName.UI_SKIN_1));
        cards.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                showCards = !showCards;
            }
        });

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        Label unitsLabel = new Label("Statistik", new Label.LabelStyle(generateFont(), Color.WHITE));
        yourTurnLabel= new Label(yourTurn, new Label.LabelStyle(generateFont(), Color.valueOf("#ff0000ff")));
        Label statisticsOpponentsLabel = new Label("Spieler", new Label.LabelStyle(generateFont(), Color.WHITE));

        //row 1
        table.add(unitsLabel).width(vp.getScreenWidth()/3f).padTop(vp.getWorldHeight() * 0.01f).padLeft(vp.getWorldWidth() * 0.02f);
        table.add(yourTurnLabel).width(vp.getScreenWidth()/3f).expandX().padTop(vp.getWorldHeight() * 0.01f);
        table.add(statisticsOpponentsLabel).expandX().right().padTop(vp.getWorldHeight() * 0.01f).padRight(vp.getWorldWidth() * 0.02f);
        table.row();
        //remaining rows
        for(int i = 0; i < playersCount; i++){
            currentPlayerLabels[i] = new Label(currentPlayerNames[i], new Label.LabelStyle(generateFont(), Color.valueOf(currentPlayerColors[i].toString())));
            occupiedTerritoriesLabel[i] = new Label( "Territorien: " + occupiedTerritoriesCount[i] + " / 42", new Label.LabelStyle(generateFont(), Color.valueOf(currentPlayerColors[i].toString())));
            table.add(occupiedTerritoriesLabel[i]).width(vp.getScreenWidth()/3f).padLeft(vp.getWorldWidth() * 0.02f);
            table.add().width(vp.getScreenWidth()/3f);
            table.add(currentPlayerLabels[i]).expandX().right().padRight(vp.getWorldWidth() * 0.02f);
            table.row();
        }
        table.row();
        table.add(cards).expandY().left().padLeft(vp.getWorldWidth() * 0.02f).bottom();
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
        font.getData().setScale((getViewport().getWorldWidth()*1.5f) / Territory.REFERENCE_WIDTH);
        generator.dispose();
        return font;
    }

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
                db.isThisPlayersTurn()) {

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

    public void setCurrentAttack(Attack attack) {
        if (attack != null) {
            String attackerName = db.getPlayerByTerritoryID(attack.getFromTerritoryID()).getNickname();
            String defenderName = db.getPlayerByTerritoryID(attack.getToTerritoryID()).getNickname();
            String fromTerritoryName = Territory.getByID(attack.getFromTerritoryID()).getTerritoryName();
            String toTerritoryName = Territory.getByID(attack.getToTerritoryID()).getTerritoryName();
            updateAttackDisplay(attackerName, defenderName, fromTerritoryName, toTerritoryName, attack.getAttackerDiceCount());
            attackDisplay.setVisible(true);
        }
        else {
            // hide attack display 3 seconds later
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    // if no new attack has been started during wait
                    if (db.getAttack() == null) {
                        attackDisplay.setVisible(false);
                    }
                }
            }, 3);
        }
    }

    private void setupAttackDisplay() {
        attackDisplay = new AttackDisplay(getScreen().getGame().getAssetManager());
        attackDisplay.setWidth(Gdx.graphics.getWidth());
        attackDisplay.setHeight(Gdx.graphics.getHeight() * 0.25f);
        attackDisplay.setY(Gdx.graphics.getHeight() * 0.75f);
        this.addActor(attackDisplay);
        attackDisplay.setOrigin(Align.center);
        attackDisplay.setVisible(false);
    }

    private void updateAttackDisplay(String attacker, String defender, String fromTerritory, String toTerritory, int armyCount) {
        attackDisplay.updateData(attacker, defender, fromTerritory, toTerritory, armyCount);
    }

    private void resetTerritoryCount(int playerColor){
        for(int i = 0; i < this.playersCount; i++){
            if(this.currentPlayerColors[i] == this.playerColors[playerColor]){
                this.occupiedTerritoriesCount[i] = 0;
            }
        }
    }

    public void setPlayerTerritoryCount(int territoryID, int playerColor){
        this.arrayT[territoryID] = Territory.getByID(territoryID).getArmyColor();
        resetTerritoryCount(playerColor);

        for (Color territoryColor : this.arrayT
             ) {
            if(territoryColor != null && territoryColor == this.playerColors[playerColor]){
                for(int i = 0; i < this.playersCount; i++){
                    if(this.currentPlayerColors[i] == territoryColor){
                        this.occupiedTerritoriesCount[i] = this.occupiedTerritoriesCount[i] + 1;
                    }
                }
            }
        }
    }

    public boolean getShowCards(){return this.showCards; }

    public void setPhaseSkipable(boolean b) {
        if (db.isThisPlayersTurn()) {
            phaseDisplay.setSkipButtonVisible(b);
        }
    }
}

