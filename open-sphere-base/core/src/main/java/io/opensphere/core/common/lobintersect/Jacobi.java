package io.opensphere.core.common.lobintersect;

import java.util.ArrayList;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * Uses the jacobi method to compute roots for a quadratic.
 */
public class Jacobi
{

    /**
     *
     * @param A - Covariance matrix
     * @param size number of rows/colulmns
     * @param eVecs - eigen vectors, output
     * @return eigen values
     */
    public static List<Double> getEigen(double[][] A, int size, DoubleMatrix2D eVecs)
    {

        double t, c, s;
        int p, q, icount, state;
        double tol = 1e-5;
        int icmax = 100;
        int[] colRowOfElMax = new int[size];
        int[] rowOfElMax = new int[1];
        double[][] temp = new double[size][size];
        double[][] D = new double[size][size];
        double[][] diagD;
        double[][] V;
        double[] maxElColRow = new double[size];
        double[] maxElRow = new double[1];
        double[][] dMinusDiagD;// = new double[size][size];
        double[][] absDminusDiagD = new double[size][size];
        double[][] rot = new double[2][2];
        double[][] rotT = new double[2][2];

        V = new double[size][size];
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                V[i][j] = 0;
                D[i][j] = A[i][j];
            }
            V[i][i] = 1.0;
        }

        // D=A;
        diagD = diag(D, size);
        dMinusDiagD = minus(D, diagD, size);
        abs(dMinusDiagD, absDminusDiagD, size);

        maxMatrix(absDminusDiagD, size, colRowOfElMax, maxElColRow);
        maxVector(maxElColRow, size, rowOfElMax, maxElRow);
        q = rowOfElMax[0];
        p = colRowOfElMax[q];
        icount = 0;
        state = 1;

        while (state == 1 && icount < icmax)
        {
            icount++;
            if (D[q][q] == D[p][p])
            {
                D[q][q] = D[p][p] + 1.e-10;
            }
            t = D[p][q] / (D[q][q] - D[p][p]);
            c = 1 / Math.sqrt(t * t + 1);
            s = c * t;
            rot[0][0] = c;
            rot[0][1] = s;
            rot[1][0] = -s;
            rot[1][1] = c;
            transpose(rot, rotT, 2);

            for (int i = 0; i < size; i++)
            {
                temp[p][i] = rotT[0][0] * D[p][i] + rotT[0][1] * D[q][i];
                temp[q][i] = rotT[1][0] * D[p][i] + rotT[1][1] * D[q][i];
                D[p][i] = temp[p][i];
                D[q][i] = temp[q][i];
            }

            for (int i = 0; i < size; i++)
            {
                temp[i][p] = D[i][p] * rot[0][0] + D[i][q] * rot[1][0];
                temp[i][q] = D[i][p] * rot[0][1] + D[i][q] * rot[1][1];
                D[i][p] = temp[i][p];
                D[i][q] = temp[i][q];
            }

            for (int i = 0; i < size; i++)
            {
                temp[i][p] = V[i][p] * rot[0][0] + V[i][q] * rot[1][0];
                temp[i][q] = V[i][p] * rot[0][1] + V[i][q] * rot[1][1];
                V[i][p] = temp[i][p];
                V[i][q] = temp[i][q];
            }

            diagD = diag(D, size);
            dMinusDiagD = minus(D, diagD, size);
            abs(dMinusDiagD, absDminusDiagD, size);
            maxMatrix(absDminusDiagD, size, colRowOfElMax, maxElColRow);
            maxVector(maxElColRow, size, rowOfElMax, maxElRow);
            q = rowOfElMax[0];
            p = colRowOfElMax[q];

            if (Math.abs(D[p][q]) < tol * Math.sqrt(sumDiagElSq(diagD, size)) / size)
            {
                state = 0;
            }
        }
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                eVecs.set(i, j, V[i][j]);
            }
        }

        List<Double> vals = new ArrayList<>(2);
        for (int i = 0; i < size; i++)
        {
            vals.add(diagD[i][i]);
        }
        return vals;
    }

    /**
     * Finds the diagonal in A and puts it in the returned matrix.
     *
     * @param A
     * @param n
     * @return matrix
     */
    public static double[][] diag(double[][] A, int n)
    {
        double[][] B = new double[n][n];
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                B[i][j] = 0;
            }
            B[i][i] = A[i][i];
        }
        return B;
    }

    /**
     * Performs a matrix subtraction.
     *
     * @param A
     * @param B
     * @param n size
     * @return Matrix
     */
    public static double[][] minus(double[][] A, double[][] B, int n)
    {
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

    /**
     * Returns a matrix of the abs value of every element of the input matrix.
     *
     * @param A
     * @param B
     * @param n
     */
    public static void abs(double[][] A, double[][] B, int n)
    {
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                B[i][j] = Math.abs(A[i][j]);
            }
        }
    }

    /**
     * Finds the max value and position for each column.
     *
     * @param A
     * @param n
     * @param Row
     * @param Max
     */
    public static void maxMatrix(double[][] A, int n, int[] Row, double[] Max)
    {
        for (int i = 0; i < n; i++)
        {
            int k = 0;
            Max[i] = A[k][i];
            Row[i] = k;
            for (int j = 0; j < n; j++)
            {
                if (A[j][i] > Max[i])
                {
                    Max[i] = A[j][i];
                    Row[i] = j;
                }
            }
            k++;
        }
    }

    /**
     * Finds the max value and it's index in A.
     *
     * @param A
     * @param n
     * @param Row
     * @param Max
     */
    public static void maxVector(double[] A, int n, int[] Row, double[] Max)
    {
        Max[0] = A[0];
        Row[0] = 0;
        for (int i = 0; i < n; i++)
        {
            if (A[i] > Max[0])
            {
                Max[0] = A[i];
                Row[0] = i;
            }
        }
    }

    /**
     * Transposes A, returns as B
     *
     * @param A
     * @param B
     * @param n
     */
    public static void transpose(double[][] A, double[][] B, int n)
    {
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                B[i][j] = A[j][i];
            }
        }
    }

    /**
     * Computes the sum of elements on the diaganol of A.
     *
     * @param A
     * @param n
     * @return sum
     */
    public static double sumDiagElSq(double[][] A, int n)
    {
        double sum = 0;
        for (int i = 0; i < n; i++)
        {
            sum = A[i][i] * A[i][i] + sum;
        }
        return sum;
    }
}
