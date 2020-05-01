package edu.aau.se2.view;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class AbstractStage extends Stage {
    private AbstractScreen screen;

    public AbstractStage(AbstractScreen screen) {
        this.screen = screen;
    }

    public AbstractStage(Viewport viewport, AbstractScreen screen) {
        super(viewport);
        this.screen = screen;
    }

    public AbstractStage(Viewport viewport, Batch batch, AbstractScreen screen) {
        super(viewport, batch);
        this.screen = screen;
    }

    public AbstractScreen getScreen() {
        return screen;
    }
}
