package io.opensphere.core.util.math;

import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Quaternion;
import io.opensphere.core.math.Vector3d;

/** Vector math utilities. */
public final class VectorUtilities
{
    /**
     * Calculates the transform for a model.
     *
     * @param model the model
     * @param heading the heading
     * @param pitch the pitch
     * @param roll the roll
     * @param scaleVector the scale vector
     * @return the transform
     */
    public static Matrix4d getModelTransform(Vector3d model, double heading, double pitch, double roll, Vector3d scaleVector)
    {
        Vector3d unitmodel = model.getNormalized();

        Vector3d north = unitmodel.cross(Vector3d.UNIT_Z).cross(unitmodel).getNormalized();
        Vector3d headVector = north.rotate(unitmodel, Math.toRadians(-heading));

        Vector3d pitchAxis = headVector.cross(unitmodel);
        Vector3d yAxis = headVector.rotate(pitchAxis, Math.toRadians(pitch));
        Vector3d zAxis = pitchAxis.cross(yAxis).rotate(yAxis, Math.toRadians(roll));
        Vector3d xAxis = new Vector3d(yAxis).cross(zAxis);

        Matrix4d transform = new Matrix4d();
        transform.setTranslation(model);
        transform.setRotationQuaternion(Quaternion.fromAxes(xAxis, yAxis, zAxis).inverse());
        transform.scale(scaleVector);

        return transform;
    }

    /** Disallow instantiation. */
    private VectorUtilities()
    {
    }
}
