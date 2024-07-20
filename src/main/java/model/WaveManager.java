package model;

import controller.GameLoop;
import model.characters.*;
import model.entities.Entity;
import model.movement.Direction;
import model.movement.Movable;
import view.menu.MainMenu;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static controller.UserInterfaceController.*;
import static controller.constants.WaveConstants.MAX_ENEMY_SPAWN_RADIUS;
import static controller.constants.WaveConstants.MIN_ENEMY_SPAWN_RADIUS;
import static model.Utils.*;
import static model.characters.GeoShapeModel.allShapeModelsList;
import static model.collision.Collidable.collidables;

public class WaveManager {
    public static final List<GeoShapeModel> waveEntities = new CopyOnWriteArrayList<>();
    public static final Random random = new Random();
    public static int wave;
    public static int PR = 0;
    private long waveStart = System.nanoTime();
    private long waveFinish;
    public static int initialWave = 0;
    public static int killedEnemies = 0;

    public void start() throws InterruptedException {
        initiateWave(initialWave);

    }

    public void lockEnemies() {
        for (GeoShapeModel model : waveEntities) {
            if (!(model instanceof EpsilonModel)) {
                model.getMovement().lockOnTarget(EpsilonModel.getINSTANCE().getModelId());
            }
        }
    }

    public int progressRateTotalWave() {
        return (int) (wave * (waveFinish - waveStart) / 1000000000);
    }

    public int progressRisk(int p) {
        return (10 * Profile.getCurrent().getCurrentGameXP() * p / EpsilonModel.getINSTANCE().getHealth());
    }

    public void randomSpawn(int wave)  {
        if (wave != 0) {
            int x = (int) EpsilonModel.getINSTANCE().getAnchor().getX();
            int y = (int) EpsilonModel.getINSTANCE().getAnchor().getY();
            while (Math.sqrt(Math.pow((x - EpsilonModel.getINSTANCE().getAnchor().getX()), 2) + Math.pow((y - EpsilonModel.getINSTANCE().getAnchor().getY()), 2)) < 250) {
                x = random.nextInt((int) EpsilonModel.getINSTANCE().getAnchor().getX() - 200, (int) EpsilonModel.getINSTANCE().getAnchor().getX() + 200);
                y = random.nextInt((int) EpsilonModel.getINSTANCE().getAnchor().getY() - 200, (int) EpsilonModel.getINSTANCE().getAnchor().getY() + 200);
            }
            new PortalModel(getMainMotionPanelId(), new Point(x, y));
        }
        for (int i = 0; i <7; i++) {
            Point location = roundPoint(addUpPoints(EpsilonModel.getINSTANCE().getAnchor(),
                    multiplyPoint(new Direction(random.nextFloat(0, 360)).getDirectionVector(),
                            random.nextFloat(MIN_ENEMY_SPAWN_RADIUS.getValue(), MAX_ENEMY_SPAWN_RADIUS.getValue()))));
            GeoShapeModel model;
            if (wave == 0) model = new SquarantineModel(location, getMainMotionPanelId());
            else {
                model = switch (random.nextInt(0, 2)) {
                    case 0 -> new SquarantineModel(location, getMainMotionPanelId());
                    case 1 -> new TrigorathModel(location, getMainMotionPanelId());
                    default -> null;
                };
            }
            if (model != null) waveEntities.add(model);
        }
    }

    private void initiateWave(int wave) throws InterruptedException {
        GameLoop.setWaveStart(System.nanoTime());
        waveFinish = System.nanoTime();
        PR += progressRisk(progressRateTotalWave());
        waveStart = System.nanoTime();
        WaveManager.wave = wave + 1;
        randomSpawn(wave);
        lockEnemies();
        Timer waveTimer = new Timer(10, null);
        waveTimer.addActionListener(e -> {
            boolean waveFinished = false;
            if (killedEnemies - 1 > wave) {
                waveFinished = true;
                killedEnemies = 0;
            }
            if (waveFinished) {
                for (Entity entity : waveEntities) {
                    if (entity instanceof GeoShapeModel geoShapeModel) {
                        allShapeModelsList.remove(this);
                        collidables.remove(geoShapeModel);
                        Movable.movables.remove(geoShapeModel);
                        eliminateView(entity.getModelId(), entity.getMotionPanelId());
                    }
                }
                waveEntities.clear();
                waveTimer.stop();
                float length = showMessage(6 - wave);
                if (wave < 6) {
                    try {
                        initiateWave(wave + 1);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } else finishGame(length);
            }
        });
        waveTimer.start();
    }

    public void finishGame(float lastSceneTime) {
        Timer timer = new Timer((int) TimeUnit.NANOSECONDS.toMillis((long) lastSceneTime), e -> {
            GameLoop.setPR(0);
            exitGame();
            Profile.getCurrent().saveXP();
            MainMenu.flushINSTANCE();
            MainMenu.getINSTANCE().togglePanel();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
