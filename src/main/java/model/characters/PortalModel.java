package model.characters;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.concurrent.TimeUnit;

import static controller.UserInterfaceController.*;
import static controller.UserInterfaceController.getMainMotionPanelId;
import static controller.constants.EntityConstants.*;

import static controller.constants.EntityConstants.EntityVertices.PORTAL_VERTICES;
import static controller.constants.EntityConstants.PointConstants.PORTAL_CENTER;
import static model.Utils.deepClone;
import static model.Utils.roundPoint;

public class PortalModel extends GeoShapeModel{
    public PortalModel( String motionPanelId) {
        super(new Point(0, 0),PORTAL_VERTICES.getValue(), PORTAL_HEALTH.getValue());
        this.setCircular(true);
        setMotionPanelId(motionPanelId);
        this.setAnchorSave(deepClone(PORTAL_CENTER.getValue()));
        assert getAnchorSave() != null;
        createPortal(getModelId(), roundPoint(getAnchorSave()), motionPanelId);
        Point2D anchor = getMotionPanelCenterLocation(getMainMotionPanelId());
        moveShapeModel(anchor);
        getMovement().setAnchor(anchor);
        Timer timer=new Timer((int) TimeUnit.SECONDS.toMillis(PORTAL_LIFE_TIME.getValue()), e -> eliminate());
        timer.setCoalesce(true);
        timer.setRepeats(false);
        timer.start();
    }
    public void setPortalLocation(Point2D location){
        moveShapeModel(location);
        getMovement().setAnchor(location);
    }
}
