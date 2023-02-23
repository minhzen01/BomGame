package bomberman;

import bomberman.entities.Entity;
import bomberman.entities.bomb.Bomb;
import bomberman.entities.bomb.Flame;
import bomberman.entities.mob.Bomber;
import bomberman.entities.mob.Mob;
import bomberman.entities.mob.enemy.Balloom;
import bomberman.entities.mob.enemy.Minvo;
import bomberman.entities.mob.enemy.Oneal;
import bomberman.entities.powerup.*;
import bomberman.entities.terrain.Brick;
import bomberman.entities.terrain.Grass;
import bomberman.entities.terrain.Wall;
import bomberman.graphics.Sprite;
import bomberman.musics.SoundEffect;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class BombermanGame extends Application {

    public static final int WIDTH = 31;
    public static final int HEIGHT = 13;

    private static int level = -1;
    private static List<Mob> mobs = new ArrayList<>();
    private static List<Bomb> bombs = new ArrayList<>();
    private static List<Entity> textures = new ArrayList<>();
    private static List<Brick> bricks = new ArrayList<>();
    private static List<PowerUp> powers = new ArrayList<>();
    private static int bombPower;
    private static int bombCourt;
    private static int speed;
    private static final char[][] map = new char[HEIGHT][WIDTH];
    private static final HashSet<String> currentlyActiveKeys = new HashSet<>();
    private static boolean endLevel = true;
    private Mob bomberman;
    private Portal portal;
    private GraphicsContext gc;
    private Canvas canvas;
    private long prevTime = 0;
    private long startTime = 0;


    public static void main(String[] args) {
        Application.launch(BombermanGame.class);
    }

    public static int getSpeed() {
        return speed;
    }

    public static void setSpeed(int speed) {
        BombermanGame.speed = speed;
    }

    public static char entityAt(int j, int i) {
        return map[i / Sprite.SCALED_SIZE][j / Sprite.SCALED_SIZE];
    }

    public static void setEntityAt(int j, int i, char c) {
        map[i / Sprite.SCALED_SIZE][j / Sprite.SCALED_SIZE] = c;
    }

    public static int getBombCourt() {
        return bombCourt;
    }

    public static void setBombCourt(int b) {
        bombCourt = b;
    }

    public static void setEnd_level(boolean e) {
        endLevel = e;
    }

    public static int getBombPower() {
        return bombPower;
    }

    public static void setBombPower(int bombPower) {
        BombermanGame.bombPower = bombPower;
    }

    public static void addBomb(Bomb b) {
        bombs.add(b);
    }

    public static HashSet<String> getKey() {
        return currentlyActiveKeys;
    }

    public static List<Mob> getMobs() {
        return mobs;
    }

    public static List<Bomb> getBombs() {
        return bombs;
    }

    public static List<Brick> getBricks() {
        return bricks;
    }

    public static List<PowerUp> getPowers() {
        return powers;
    }

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(Sprite.SCALED_SIZE * WIDTH, Sprite.SCALED_SIZE * HEIGHT);
        gc = canvas.getGraphicsContext2D();

        Group root = new Group();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, Sprite.SCALED_SIZE * WIDTH, Sprite.SCALED_SIZE * HEIGHT);

        stage.setScene(scene);
        stage.show();

        scene.setOnKeyPressed(keyEvent -> currentlyActiveKeys.add(keyEvent.getCode().toString()));

        scene.setOnKeyReleased(keyEvent -> currentlyActiveKeys.remove(keyEvent.getCode().toString()));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (endLevel) {
                    if (startTime == 0) {
                        level++;
                        startTime = System.currentTimeMillis();
                    }
                    if (System.currentTimeMillis() - startTime <= 2000) {
                        gc.setFill(Color.BLACK);
                        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.setTextBaseline(VPos.CENTER);
                        gc.setFill(Color.WHITE);
                        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 50));
                        gc.fillText(
                                "Level " + level,
                                Math.round(canvas.getWidth() / 2),
                                Math.round(canvas.getHeight() / 2)
                        );
                    } else {
                        startTime = 0;
                        prevTime = System.currentTimeMillis();
                        createMap();
                        endLevel = false;
                    }
                } else {
                    if (bomberman.isRemoved()) {
                        SoundEffect.start.stop();
                        restart();
                    } else {
                        play(stage);
                    }
                }
            }
        };
        timer.start();

    }

    public void update() {

        bomberman.setSpeed(speed);

        if (portal != null) {
            if (mobs.size() != 1) {
                setEntityAt(portal.getX(), portal.getY(), '*');
            } else {
                setEntityAt(portal.getX(), portal.getY(), ' ');
            }
        }

        for (int i = 0; i < mobs.size(); i++) {
            Mob m = mobs.get(i);
            m.update();
            if (m.isRemoved()) {
                m.kill();
                if (System.currentTimeMillis() - m.getDeadTime() >= 1000) {
                    mobs.remove(m);
                }
            }
        }

        for (int i = 0; i < bombs.size(); i++) {
            Bomb b = bombs.get(i);
            b.update();
            if (b.isRemoved()) {
                if (!(b instanceof Flame)) {
                    b.explode();
                    setEntityAt(b.getX(), b.getY(), ' ');
                    bombCourt++;
                }
                bombs.remove(b);
            }
        }

        for (int i = 0; i < bricks.size(); i++) {
            Brick b = bricks.get(i);
            b.update();
            if (b.isRemoved()) {
                b.kill();
                if (b.getHiddenEntity() != null) {
                    powers.add(b.getHiddenEntity());
                    if (b.getHiddenEntity() instanceof Portal) {
                        setEntityAt(b.getX(), b.getY(), '*');
                        portal = new Portal(b.getX(), b.getY(), Sprite.portal.getFxImage());
                    } else {
                        setEntityAt(b.getX(), b.getY(), ' ');
                    }
                    b.setHiddenEntity(null);
                } else {
                    textures.add(new Grass(b.getX(), b.getY(), Sprite.grass.getFxImage()));
                }
                if (System.currentTimeMillis() - b.getBreakTime() >= 500) {
                    bricks.remove(b);
                }
            }
        }

        for (int i = 0; i < powers.size(); i++) {
            PowerUp p = powers.get(i);
            if (p.isRemoved()) {
                powers.remove(p);
                textures.add(new Grass(p.getX(), p.getY(), Sprite.grass.getFxImage()));
            }
        }
    }

    public void creatmapfromfile() {
        String url = "res/levels/Level" + level + ".txt";

        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(url);
            Scanner sc = new Scanner(fileInputStream);
            sc.nextLine();

            int curLine = 0;
            while (sc.hasNextLine() && curLine < HEIGHT) {
                String line = sc.nextLine();
                for (int i = 0; i < line.length(); i++)
                    map[curLine][i] = line.charAt(i);
                curLine++;
            }
        } catch (FileNotFoundException ignored) {

        }
    }

    public void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        textures.forEach(g -> g.render(gc));
        bricks.forEach(g -> g.render(gc));
        powers.forEach(g -> g.render(gc));
        mobs.forEach(g -> g.render(gc));
        bombs.forEach(g -> g.render(gc));
    }

    public void play(Stage stage) {
        double delta;
        long curTime = System.currentTimeMillis();
        delta = (double) (curTime - prevTime) / 1000.0;
        double fps = 1 / delta;

        stage.setTitle("BomberMan | FPS: " + ((int) fps));
        while (delta > 0) {
            render();
            update();
            delta -= 1;
        }
        prevTime = curTime;
    }

    public void restart() {
        render();
        update();
        if (System.currentTimeMillis() - prevTime >= 1500) {
            SoundEffect.death.stop();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 50));
            gc.fillText(
                    "You are dead!",
                    Math.round(canvas.getWidth() / 2),
                    Math.round(canvas.getHeight() / 2)
            );
        }
        if (System.currentTimeMillis() - prevTime >= 3000) {
            startTime = 0;
            createMap();
            render();
        }
    }

    public void createMap() {
        SoundEffect.start.play();
        mobs = new ArrayList<>();
        bombs = new ArrayList<>();
        textures = new ArrayList<>();
        powers = new ArrayList<>();
        bricks = new ArrayList<>();
        portal = null;
        setBombPower(1);
        setBombCourt(1);
        setSpeed(2);
        creatmapfromfile();

        int i = 0;
        while (i < HEIGHT) {
            for (int j = 0; j < WIDTH; j++) {
                Entity e;

                switch (map[i][j]) {
                    case '#': {
                        e = new Wall(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.wall.getFxImage());
                        textures.add(e);
                        break;
                    }
                    case '*': {
                        Brick b = new Brick(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.brick.getFxImage());
                        bricks.add(b);
                        break;
                    }
                    case 'x': {
                        map[i][j] = '*';
                        Brick b = new Brick(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.brick.getFxImage());
                        b.setHiddenEntity(new Portal(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.portal.getFxImage()));
                        bricks.add(b);
                        break;
                    }
                    case 'f': {
                        map[i][j] = '*';
                        Brick b = new Brick(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.brick.getFxImage());
                        b.setHiddenEntity(new PowerItem(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.powerup_flames.getFxImage()));
                        bricks.add(b);
                        break;
                    }
                    case 'b': {
                        map[i][j] = '*';
                        Brick b = new Brick(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.brick.getFxImage());
                        b.setHiddenEntity(new BombItem(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.powerup_bombs.getFxImage()));
                        bricks.add(b);
                        break;
                    }
                    case 's': {
                        map[i][j] = '*';
                        Brick b = new Brick(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.brick.getFxImage());
                        b.setHiddenEntity(new SpeedItem(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.powerup_speed.getFxImage()));
                        bricks.add(b);
                        break;
                    }
                    case '1': {
                        map[i][j] = ' ';
                        mobs.add(new Balloom(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.balloom_right1.getFxImage()));
                        e = new Grass(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.grass.getFxImage());
                        textures.add(e);
                        break;
                    }
                    case '2': {
                        map[i][j] = ' ';
                        mobs.add(new Oneal(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.oneal_right1.getFxImage()));
                        e = new Grass(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.grass.getFxImage());
                        textures.add(e);
                        break;
                    }
                    case '3': {
                        map[i][j] = ' ';
                        mobs.add(new Minvo(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.minvo_right1.getFxImage()));
                        e = new Grass(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.grass.getFxImage());
                        textures.add(e);
                        break;
                    }
                    case 'p': {
                        map[i][j] = ' ';
                        bomberman = new Bomber(i * Sprite.SCALED_SIZE, j * Sprite.SCALED_SIZE, Sprite.player_right.getFxImage());
                        mobs.add(bomberman);
                        e = new Grass(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.grass.getFxImage());
                        textures.add(e);
                        break;
                    }

                    default: {
                        e = new Grass(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.grass.getFxImage());
                        textures.add(e);
                        break;
                    }
                }
            }
            i++;
        }
    }
}

