package controller;

import model.WaveManager;
import model.characters.*;
import model.movement.Direction;

import java.awt.*;

import static controller.UserInterfaceController.getMainMotionPanelId;
import static controller.constants.WaveConstants.MAX_ENEMY_SPAWN_RADIUS;
import static controller.constants.WaveConstants.MIN_ENEMY_SPAWN_RADIUS;
import static model.Utils.*;
import static model.WaveManager.*;

public class SpawnThread extends Thread {
    private final WaveManager waveManager;
    private final int wave;

    public SpawnThread(WaveManager waveManager, int wave) {
        this.waveManager = waveManager;
        this.wave = wave;
    }

    @Override
    public void run() {
        if (wave != 0) {
            int x = (int) EpsilonModel.getINSTANCE().getAnchor().getX();
            int y = (int) EpsilonModel.getINSTANCE().getAnchor().getY();
            while (Math.sqrt(Math.pow((x - EpsilonModel.getINSTANCE().getAnchor().getX()), 2) + Math.pow((y - EpsilonModel.getINSTANCE().getAnchor().getY()), 2)) < 250) {
                x = random.nextInt((int) EpsilonModel.getINSTANCE().getAnchor().getX() - 200, (int) EpsilonModel.getINSTANCE().getAnchor().getX() + 200);
                y = random.nextInt((int) EpsilonModel.getINSTANCE().getAnchor().getY() - 200, (int) EpsilonModel.getINSTANCE().getAnchor().getY() + 200);
            }
            new PortalModel(getMainMotionPanelId(), new Point(x, y));
        }

        Point location = roundPoint(addUpPoints(EpsilonModel.getINSTANCE().getAnchor(),
                multiplyPoint(new Direction(random.nextFloat(0, 360)).getDirectionVector(),
                        random.nextFloat(MIN_ENEMY_SPAWN_RADIUS.getValue(), MAX_ENEMY_SPAWN_RADIUS.getValue()))));
        GeoShapeModel model = switch (random.nextInt(0, 2)) {
            case 0 -> new SquarantineModel(location, getMainMotionPanelId());
            case 1 -> new TrigorathModel(location, getMainMotionPanelId());
            default -> null;
        };

    }
}
