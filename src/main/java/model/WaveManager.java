package model;

import model.characters.*;
import model.entities.Entity;
import model.movement.Direction;
import view.menu.MainMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static controller.UserInterfaceController.*;
import static controller.constants.EntityConstants.COLLECTIBLE_HEALTH;
import static controller.constants.EntityConstants.EntityVertices.COLLECTIBLE_VERTICES;
import static controller.constants.WaveConstants.MAX_ENEMY_SPAWN_RADIUS;
import static controller.constants.WaveConstants.MIN_ENEMY_SPAWN_RADIUS;
import static model.Utils.*;
import static model.characters.CollectibleModel.bulkCreateCollectibles;

public class WaveManager {
    public final List<Integer> waveCount = Profile.getCurrent().getWaveEnemyCount();
    private final List<GeoShapeModel> waveEntities = new CopyOnWriteArrayList<>();
    public static final Random random = new Random();
    public static int wave;

    public void start() {
        initiateWave(0);
    }

    public void lockEnemies() {
        for (GeoShapeModel model : waveEntities) {
            if (!(model instanceof EpsilonModel)) {
                model.getMovement().lockOnTarget(EpsilonModel.getINSTANCE().getModelId());
            }
        }
    }

    public void randomSpawn(int wave) {
        if (wave!=0) {
            PortalModel portalModel = new PortalModel(getMainMotionPanelId());
            portalModel.setPortalLocation(new Point(500, 400));
            bulkCreateCollectibles(portalModel);
        }
        for (int i = 0; i < waveCount.get(wave); i++) {
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

    private void initiateWave(int wave) {
        WaveManager.wave = wave + 1;
        randomSpawn(wave);
        lockEnemies();
        Timer waveTimer = new Timer(10, null);
        waveTimer.addActionListener(e -> {
            boolean waveFinished = true;
            for (GeoShapeModel shapeModel : waveEntities) {
                if (shapeModel.getHealth() > 0) {
                    waveFinished = false;
                    break;
                }
            }
            if (waveFinished) {
                waveTimer.stop();
                waveEntities.clear();
                float length = showMessage(waveCount.size() - 1 - wave);
                if (wave < waveCount.size() - 1) initiateWave(wave + 1);
                else finishGame(length);
            }
        });
        waveTimer.start();
    }

    public void finishGame(float lastSceneTime) {
        Timer timer = new Timer((int) TimeUnit.NANOSECONDS.toMillis((long) lastSceneTime), e -> {
            exitGame();
            Profile.getCurrent().saveXP();
            MainMenu.flushINSTANCE();
            MainMenu.getINSTANCE().togglePanel();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
