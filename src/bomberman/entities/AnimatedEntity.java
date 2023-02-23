package bomberman.entities;

import javafx.scene.image.Image;

public abstract class AnimatedEntity extends Entity {

    public AnimatedEntity(int x, int y, Image img) {
        super(x, y, img);
    }

    @Override
    public void update() {
    }
}
