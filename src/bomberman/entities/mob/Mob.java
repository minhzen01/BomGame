package bomberman.entities.mob;

import bomberman.BombermanGame;
import bomberman.entities.AnimatedEntity;
import bomberman.entities.Entity;
import bomberman.graphics.Sprite;
import javafx.scene.image.Image;

import java.util.ArrayList;

public abstract class Mob extends AnimatedEntity {

    protected int animate = 0;
    private int direction = -1;
    private int speed = BombermanGame.getSpeed();
    private int fat = Sprite.SCALED_SIZE;
    private long deadTime = -1;
    private boolean movethruBomb = false;
    private boolean movethruBrick = false;

    public Mob(int x, int y, Image img) {
        super(x, y, img);
    }

    public boolean isMovethruBomb() {
        return movethruBomb;
    }

    public void setMovethruBomb(boolean movethruBomb) {
        this.movethruBomb = movethruBomb;
    }

    public long getDeadTime() {
        return deadTime;
    }

    public void setDeadTime(long deadTime) {
        this.deadTime = deadTime;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isMovethruBrick() {
        return movethruBrick;
    }

    public void setMovethruBrick(boolean movethruBrick) {
        this.movethruBrick = movethruBrick;
    }

    @Override
    public void update() {

    }

    public abstract void move();

    public boolean canMove(int x, int y) {
        switch (BombermanGame.entityAt(x, y)) {
            case '#':
                return false;
        }
        if (BombermanGame.entityAt(x, y) == '*' && !isMovethruBrick()) {
            return false;
        }
        return BombermanGame.entityAt(x, y) != '.' || isMovethruBomb();
    }
    public abstract void kill();

    public boolean standOnObject(Entity b) {
        if ((((b.getX() / Sprite.SCALED_SIZE) == (x / Sprite.SCALED_SIZE)
                || (b.getX() / Sprite.SCALED_SIZE) == ((x + getFat() - 1) / Sprite.SCALED_SIZE))
                && ((b.getY() / Sprite.SCALED_SIZE) == ((y + Sprite.SCALED_SIZE / 8) / Sprite.SCALED_SIZE)
                || (b.getY() / Sprite.SCALED_SIZE) == ((y + Sprite.SCALED_SIZE - 1) / Sprite.SCALED_SIZE))
        )) return true;
        else return false;
    }

    public void randomMove() {
        ArrayList<Integer> _direction = new ArrayList<>();
        if (canMove(x - getSpeed(), y)) _direction.add(1);
        if (canMove(x + getFat() - 1 + getSpeed(), y)) _direction.add(0);
        if (canMove(x, y + Sprite.SCALED_SIZE - 1 + getSpeed())) _direction.add(3);
        if (canMove(x, y - getSpeed())) _direction.add(2);
        if (_direction.size() == 0) {
            setDirection(-1);
        } else {
            double index = Math.random() * (_direction.size());
            setDirection(_direction.get((int) index));
        }
    }
}
