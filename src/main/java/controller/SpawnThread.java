package controller;


import model.WaveManager;
import model.characters.*;
import model.movement.Direction;

import java.awt.*;

import static controller.AudioHandler.random;
import static controller.UserInterfaceController.getMainMotionPanelId;
import static controller.constants.WaveConstants.MAX_ENEMY_SPAWN_RADIUS;
import static controller.constants.WaveConstants.MIN_ENEMY_SPAWN_RADIUS;
import static model.Utils.*;

public class SpawnThread extends Thread {
    private final int wave;



    public void setRunning(boolean running) {
        this.running = running;
    }

    private volatile boolean running = true;

    public SpawnThread(int wave) {
        this.wave = wave;
    }


    @Override
    public void run() {
        while (running) {
            if (WaveManager.wave>5){
                break;
            }
            try {
                sleep(((WaveManager.waveEntities.size()+1)/(2 *1*(wave+1)))* 4000);
                Point location = roundPoint(addUpPoints(EpsilonModel.getINSTANCE().getAnchor(),
                        multiplyPoint(new Direction(random.nextFloat(0, 360)).getDirectionVector(),
                                random.nextFloat(MIN_ENEMY_SPAWN_RADIUS.getValue(), MAX_ENEMY_SPAWN_RADIUS.getValue()))));
                GeoShapeModel model;
                if (wave == 0) {
                    model = new SquarantineModel(location, getMainMotionPanelId());
                } else {
                    model = switch (random.nextInt(0, 2)) {
                        case 0 -> new SquarantineModel(location, getMainMotionPanelId());
                        case 1 -> new TrigorathModel(location, getMainMotionPanelId());
                        default -> null;
                    };
                }
                if (model != null) {
                    WaveManager.waveEntities.add(model);
                    model.getMovement().lockOnTarget(EpsilonModel.getINSTANCE().getModelId());

                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
    public void stopThread() {
        running = false;
    }
}
