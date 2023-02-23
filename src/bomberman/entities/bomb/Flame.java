package bomberman.entities.bomb;

import bomberman.BombermanGame;
import bomberman.entities.mob.Bomber;
import bomberman.entities.mob.Mob;
import bomberman.entities.terrain.Brick;
import bomberman.graphics.Sprite;
import bomberman.musics.SoundEffect;
import java.util.List;

public class Flame extends Bomb {
    private final Sprite flame1;
    private final Sprite flame2;
    private final Sprite flame3;

    public Flame(int x, int y, Sprite flame1, Sprite flame2, Sprite flame3) {
        super(x, y, flame1.getFxImage());
        this.flame1 = flame1;
        this.flame2 = flame2;
        this.flame3 = flame3;
        BombermanGame.setEntityAt(x, y, ' ');
        BombermanGame.setBombCourt(BombermanGame.getBombCourt() + 1);
    }

    @Override
    public void update() {
        aliveTime = System.currentTimeMillis() - startTime;
        if (aliveTime >= 500) {
            remove = true;
        } else {
            List<Brick> bricks = BombermanGame.getBricks();
            for (Brick brick : bricks) {
                if (brick.getX() == x && brick.getY() == y) {
                    this.setRemove(true);
                    brick.setRemove(true);
                }
            }

            List<Mob> mobs = BombermanGame.getMobs();
            for (Mob m : mobs) {
                if (m.standOnObject(this)) {
                    m.setRemove(true);
                    if (m instanceof Bomber) {
                        SoundEffect.death.play();
                    }
                }
            }

            List<Bomb> bombs = BombermanGame.getBombs();
            for (Bomb b : bombs) {
                if (!(b instanceof Flame) && b.getX() == x && b.getY() == y) {
                    b.setRemove(true);
                }
            }
        }

        img = Sprite.movingSprite(flame1, flame2, flame3, aliveTime, 500).getFxImage();
    }
}
