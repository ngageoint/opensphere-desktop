package io.opensphere.core.geometry.renderproperties;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.math.AbstractMatrix;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTFloatArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.lang.Pair;

/** Default properties for using a fragment shader during rendering. */
public class DefaultFragmentShaderProperties extends AbstractRenderProperties implements FragmentShaderProperties
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Uniform names and values of type boolean. */
    private transient Collection<? extends Pair<String, PetrifyableTIntArrayList>> myBooleanUniforms;

    /** Uniform names and values of type float. */
    private transient Collection<? extends Pair<String, PetrifyableTFloatArrayList>> myFloatUniforms;

    /** Uniform names and values of type int. */
    private transient Collection<? extends Pair<String, PetrifyableTIntArrayList>> myIntegerUniforms;

    /** Uniform names and values for matrices. */
    private transient Collection<? extends Pair<String, AbstractMatrix>> myMatrixUniforms;

    /** The shader program or snippet as appropriate for the shader type. */
    private String myShaderCode;

    @Override
    public Collection<? extends Pair<String, PetrifyableTIntArrayList>> getBooleanUniforms()
    {
        return myBooleanUniforms;
    }

    @Override
    public Collection<? extends Pair<String, PetrifyableTFloatArrayList>> getFloatUniforms()
    {
        return myFloatUniforms;
    }

    @Override
    public Collection<? extends Pair<String, PetrifyableTIntArrayList>> getIntegerUniforms()
    {
        return myIntegerUniforms;
    }

    @Override
    public Collection<? extends Pair<String, AbstractMatrix>> getMatrixUniforms()
    {
        return Collections.unmodifiableCollection(myMatrixUniforms);
    }

    @Override
    public String getShaderCode()
    {
        return myShaderCode;
    }

    @Override
    public void setupShader(ShaderPropertiesSet propertiesSet)
    {
        if (propertiesSet.getShaderCode() == null)
        {
            throw new InvalidParameterException(
                    "Attempted to set shader code to null. The shader properties must include shader code.");
        }
        myBooleanUniforms = propertiesSet.getBooleanUniforms();
        myFloatUniforms = propertiesSet.getFloatUniforms();
        myIntegerUniforms = propertiesSet.getIntegerUniforms();
        myMatrixUniforms = propertiesSet.getMatrixUniforms();
        myShaderCode = propertiesSet.getShaderCode();
        notifyChanged();
    }

    /**
     * De-serialize the shader properties from a stream.
     *
     * @param s The stream.
     * @throws IOException If there is an error.
     * @throws ClassNotFoundException If a   class is not found.
     */
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();

        ShaderPropertiesSet propertiesSet = new ShaderPropertiesSet();
        propertiesSet.setShaderCode(getShaderCode());

        int len;

        len = s.readInt();
        Collection<Pair<String, boolean[]>> booleanUniforms = New.collection(len);
        for (int index = 0; index < len; ++index)
        {
            booleanUniforms.add(Pair.create((String)s.readObject(), (boolean[])s.readObject()));
        }
        propertiesSet.setBooleanUniforms(booleanUniforms);

        len = s.readInt();
        Collection<Pair<String, float[]>> floatUniforms = New.collection(len);
        for (int index = 0; index < len; ++index)
        {
            floatUniforms.add(Pair.create((String)s.readObject(), (float[])s.readObject()));
        }
        propertiesSet.setFloatUniforms(floatUniforms);

        len = s.readInt();
        Collection<Pair<String, int[]>> integerUniforms = New.collection(len);
        for (int index = 0; index < len; ++index)
        {
            integerUniforms.add(Pair.create((String)s.readObject(), (int[])s.readObject()));
        }
        propertiesSet.setIntegerUniforms(integerUniforms);

        len = s.readInt();
        Collection<Pair<String, AbstractMatrix>> matrixUniforms = New.collection(len);
        for (int index = 0; index < len; ++index)
        {
            matrixUniforms.add(Pair.create((String)s.readObject(), (AbstractMatrix)s.readObject()));
        }
        propertiesSet.setMatrixUniforms(matrixUniforms);

        setupShader(propertiesSet);
    }

    /**
     * Serialize the shader properties to a stream.
     *
     * @param s The stream.
     * @throws java.io.IOException If there is an error.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException
    {
        s.defaultWriteObject();

        s.writeInt(myBooleanUniforms.size());
        for (Pair<String, PetrifyableTIntArrayList> pair : myBooleanUniforms)
        {
            s.writeObject(pair.getFirstObject());
            PetrifyableTIntArrayList intArr = pair.getSecondObject();
            boolean[] boolArr = new boolean[intArr.size()];
            for (int index = 0; index < boolArr.length; ++index)
            {
                boolArr[index] = intArr.get(index) != 0;
            }

            s.writeObject(boolArr);
        }
        s.writeInt(myFloatUniforms.size());
        for (Pair<String, PetrifyableTFloatArrayList> pair : myFloatUniforms)
        {
            s.writeObject(pair.getFirstObject());
            s.writeObject(pair.getSecondObject().toArray());
        }
        s.writeInt(myIntegerUniforms.size());
        for (Pair<String, PetrifyableTIntArrayList> pair : myIntegerUniforms)
        {
            s.writeObject(pair.getFirstObject());
            s.writeObject(pair.getSecondObject().toArray());
        }
        s.writeInt(myMatrixUniforms.size());
        for (Pair<String, AbstractMatrix> pair : myMatrixUniforms)
        {
            s.writeObject(pair.getFirstObject());
            s.writeObject(pair.getSecondObject());
        }
    }
}
