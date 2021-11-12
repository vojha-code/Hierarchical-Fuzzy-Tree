package Randoms;

import java.io.*;

public class MersenneTwisterFast implements Serializable, Cloneable
    {
    // Serialization
    private static final long serialVersionUID = -8219700664442619525L;  // locked as of Version 15
    
    // Period parameters
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908b0df;   //    private static final * constant vector a
    private static final int UPPER_MASK = 0x80000000; // most significant w-r bits
    private static final int LOWER_MASK = 0x7fffffff; // least significant r bits


    // Tempering parameters
    private static final int TEMPERING_MASK_B = 0x9d2c5680;
    private static final int TEMPERING_MASK_C = 0xefc60000;
    
    private int mt[]; // the array for the state vector
    private int mti; // mti==N+1 means mt[N] is not initialized
    private int mag01[];
    
    // a good initial seed (of int size, though stored in a long)
    //private static final long GOOD_SEED = 4357;

    private double __nextNextGaussian;
    private boolean __haveNextNextGaussian;
    private boolean isAuto = false;
    
    /* We're overriding all internal data, to my knowledge, so this should be okay */
    public Object clone()
        {
        try
            {
            MersenneTwisterFast f = (MersenneTwisterFast)(super.clone());
            f.mt = (int[])(mt.clone());
            f.mag01 = (int[])(mag01.clone());
            return f;
            }
        catch (CloneNotSupportedException e) { throw new InternalError(); } // should never happen
        }
    
    /** Returns true if the MersenneTwisterFast's current internal state is equal to another MersenneTwisterFast. 
        This is roughly the same as equals(other), except that it compares based on value but does not
        guarantee the contract of immutability (obviously random number generators are immutable).
        Note that this does NOT check to see if the internal gaussian storage is the same
        for both.  You can guarantee that the internal gaussian storage is the same (and so the
        nextGaussian() methods will return the same values) by calling clearGaussian() on both
        objects. */
    public boolean stateEquals(MersenneTwisterFast other)
        {
        if (other == this) return true;
        if (other == null)return false;

        if (mti != other.mti) return false;
        for(int x=0;x<mag01.length;x++)
            if (mag01[x] != other.mag01[x]) return false;
        for(int x=0;x<mt.length;x++)
            if (mt[x] != other.mt[x]) return false;
        return true;
        }

    /** Reads the entire state of the MersenneTwister RNG from the stream */
    public void readState(DataInputStream stream) throws IOException
        {
        int len = mt.length;
        for(int x=0;x<len;x++) mt[x] = stream.readInt();
        
        len = mag01.length;
        for(int x=0;x<len;x++) mag01[x] = stream.readInt();
        
        mti = stream.readInt();
        __nextNextGaussian = stream.readDouble();
        __haveNextNextGaussian = stream.readBoolean();
        }
        
    /** Writes the entire state of the MersenneTwister RNG to the stream */
    public void writeState(DataOutputStream stream) throws IOException
        {
        int len = mt.length;
        for(int x=0;x<len;x++) stream.writeInt(mt[x]);
        
        len = mag01.length;
        for(int x=0;x<len;x++) stream.writeInt(mag01[x]);
        
        stream.writeInt(mti);
        stream.writeDouble(__nextNextGaussian);
        stream.writeBoolean(__haveNextNextGaussian);
        }

    /**
     * Constructor using the default seed.
     */
    public MersenneTwisterFast()
        {
        this(System.currentTimeMillis());
        }
    
    /**
     * Constructor using a given seed.  Though you pass this seed in
     * as a long, it's best to make sure it's actually an integer.
     *
     */
    public MersenneTwisterFast(long seed)
        {
        setSeed(seed);
        }
    
    /**
     * Constructor using an array of integers as seed.
     * Your array must have a non-zero length.  Only the first 624 integers
     * in the array are used; if the array is shorter than this then
     * integers are repeatedly used in a wrap-around fashion.
     */
    public MersenneTwisterFast(int[] array)
        {
        setSeed(array);
        }

    /**
     * Initalize the pseudo random number generator.  Don't
     * pass in a long that's bigger than an int (Mersenne Twister
     * only uses the first 32 bits for its seed).   
     */

    public void setSeed(long seed)
        {
        // Due to a bug in java.util.Random clear up to 1.2, we're
        // doing our own Gaussian variable.
        __haveNextNextGaussian = false;

        mt = new int[N];
        
        mag01 = new int[2];
        mag01[0] = 0x0;
        mag01[1] = MATRIX_A;

        mt[0]= (int)(seed & 0xffffffff);
        for (mti=1; mti<N; mti++) 
            {
            mt[mti] = 
                (1812433253 * (mt[mti-1] ^ (mt[mti-1] >>> 30)) + mti); 
            /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
            /* In the previous versions, MSBs of the seed affect   */
            /* only MSBs of the array mt[].                        */
            /* 2002/01/09 modified by Makoto Matsumoto             */
            // mt[mti] &= 0xffffffff;
            /* for >32 bit machines */
            }
        }
    /**
     * Sets the seed of the MersenneTwister using an array of integers.
     * Your array must have a non-zero length.  Only the first 624 integers
     * in the array are used; if the array is shorter than this then
     * integers are repeatedly used in a wrap-around fashion.
     */

    public void setSeed(int[] array)
        {
        if (array.length == 0)
            throw new IllegalArgumentException("Array length must be greater than zero");
        int i, j, k;
        setSeed(19650218);
        i=1; j=0;
        k = (N>array.length ? N : array.length);
        for (; k!=0; k--) 
            {
            mt[i] = (mt[i] ^ ((mt[i-1] ^ (mt[i-1] >>> 30)) * 1664525)) + array[j] + j; /* non linear */
            // mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
            i++;
            j++;
            if (i>=N) { mt[0] = mt[N-1]; i=1; }
            if (j>=array.length) j=0;
            }
        for (k=N-1; k!=0; k--) 
            {
            mt[i] = (mt[i] ^ ((mt[i-1] ^ (mt[i-1] >>> 30)) * 1566083941)) - i; /* non linear */
            // mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
            i++;
            if (i>=N) 
                {
                mt[0] = mt[N-1]; i=1; 
                }
            }
        mt[0] = 0x80000000; /* MSB is 1; assuring non-zero initial array */ 
        }


    public int nextInt()
        {
        int y;
        
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        return y;
        }

    public short nextShort()
        {
        int y;
        
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        return (short)(y >>> 16);
        }

    public char nextChar()
        {
        int y;
        
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        return (char)(y >>> 16);
        }

    public boolean nextBoolean()
        {
        int y;
        
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        return (boolean)((y >>> 31) != 0);
        }

    /** This generates a coin flip with a probability <tt>probability</tt>
        of returning true, else returning false.  <tt>probability</tt> must
        be between 0.0 and 1.0, inclusive.   Not as precise a random real
        event as nextBoolean(double), but twice as fast. To explicitly
        use this, remember you may need to cast to float first. */

    public boolean nextBoolean(float probability)
        {
        int y;
        
        if (probability < 0.0f || probability > 1.0f)
            throw new IllegalArgumentException ("probability must be between 0.0 and 1.0 inclusive.");
        if (probability==0.0f) return false;            // fix half-open issues
        else if (probability==1.0f) return true;        // fix half-open issues
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        return (y >>> 8) / ((float)(1 << 24)) < probability;
        }

    /** This generates a coin flip with a probability <tt>probability</tt>
        of returning true, else returning false.  <tt>probability</tt> must
        be between 0.0 and 1.0, inclusive. */

    public boolean nextBoolean(double probability)
        {
        int y;
        int z;

        if (probability < 0.0 || probability > 1.0)
            throw new IllegalArgumentException ("probability must be between 0.0 and 1.0 inclusive.");
        if (probability==0.0) return false;             // fix half-open issues
        else if (probability==1.0) return true; // fix half-open issues
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
            z = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (z >>> 1) ^ mag01[z & 0x1];
            
            mti = 0;
            }
        
        z = mt[mti++];
        z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
        z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
        z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
        z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)
        
        /* derived from nextDouble documentation in jdk 1.2 docs, see top */
        return ((((long)(y >>> 6)) << 27) + (z >>> 5)) / (double)(1L << 53) < probability;
        }

    public byte nextByte()
        {
        int y;
        
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        return (byte)(y >>> 24);
        }

    public void nextBytes(byte[] bytes)
        {
        int y;
        
        for (int x=0;x<bytes.length;x++)
            {
            if (mti >= N)   // generate N words at one time
                {
                int kk;
                final int[] mt = this.mt; // locals are slightly faster 
                final int[] mag01 = this.mag01; // locals are slightly faster 
                
                for (kk = 0; kk < N - M; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                for (; kk < N-1; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];
                
                mti = 0;
                }
            
            y = mt[mti++];
            y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

            bytes[x] = (byte)(y >>> 24);
            }
        }

    public long nextLong()
        {
        int y;
        int z;

        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
            z = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (z >>> 1) ^ mag01[z & 0x1];
            
            mti = 0;
            }
        
        z = mt[mti++];
        z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
        z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
        z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
        z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)
        
        return (((long)y) << 32) + (long)z;
        }

    /** Returns a long drawn uniformly from 0 to n-1.  Suffice it to say,
        n must be > 0, or an IllegalArgumentException is raised. */
    public long nextLong(long n)
        {
        if (n<=0)
            throw new IllegalArgumentException("n must be positive, got: " + n);
        
        long bits, val;
        do 
            {
            int y;
            int z;
    
            if (mti >= N)   // generate N words at one time
                {
                int kk;
                final int[] mt = this.mt; // locals are slightly faster 
                final int[] mag01 = this.mag01; // locals are slightly faster 
            
                for (kk = 0; kk < N - M; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                for (; kk < N-1; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];
    
                mti = 0;
                }
    
            y = mt[mti++];
            y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)
    
            if (mti >= N)   // generate N words at one time
                {
                int kk;
                final int[] mt = this.mt; // locals are slightly faster 
                final int[] mag01 = this.mag01; // locals are slightly faster 
                
                for (kk = 0; kk < N - M; kk++)
                    {
                    z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+M] ^ (z >>> 1) ^ mag01[z & 0x1];
                    }
                for (; kk < N-1; kk++)
                    {
                    z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+(M-N)] ^ (z >>> 1) ^ mag01[z & 0x1];
                    }
                z = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N-1] = mt[M-1] ^ (z >>> 1) ^ mag01[z & 0x1];
                
                mti = 0;
                }
            
            z = mt[mti++];
            z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
            z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
            z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
            z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)
            
            bits = (((((long)y) << 32) + (long)z) >>> 1);
            val = bits % n;
            } while (bits - val + (n-1) < 0);
        return val;
        }

    /** Returns a random double in the half-open range from [0.0,1.0).  Thus 0.0 is a valid
        result but 1.0 is not. */
    public double nextDouble()
        {
        int y;
        int z;

        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (z >>> 1) ^ mag01[z & 0x1];
                }
            z = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (z >>> 1) ^ mag01[z & 0x1];
            
            mti = 0;
            }
        
        z = mt[mti++];
        z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
        z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
        z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
        z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)
        
        /* derived from nextDouble documentation in jdk 1.2 docs, see top */
        return ((((long)(y >>> 6)) << 27) + (z >>> 5)) / (double)(1L << 53);
        }

    /** Returns a double in the range from 0.0 to 1.0, possibly inclusive of 0.0 and 1.0 themselves.  Thus:

        <p><table border=0>
        <th><td>Expression<td>Interval
        <tr><td>nextDouble(false, false)<td>(0.0, 1.0)
        <tr><td>nextDouble(true, false)<td>[0.0, 1.0)
        <tr><td>nextDouble(false, true)<td>(0.0, 1.0]
        <tr><td>nextDouble(true, true)<td>[0.0, 1.0]
        </table>
        
        <p>This version preserves all possible random values in the double range.
    */
    public double nextDouble(boolean includeZero, boolean includeOne)
        {
        double d = 0.0;
        do
            {
            d = nextDouble();                           // grab a value, initially from half-open [0.0, 1.0)
            if (includeOne && nextBoolean()) d += 1.0;  // if includeOne, with 1/2 probability, push to [1.0, 2.0)
            } 
        while ( (d > 1.0) ||                            // everything above 1.0 is always invalid
            (!includeZero && d == 0.0));            // if we're not including zero, 0.0 is invalid
        return d;
        }

    /** 
        Clears the internal gaussian variable from the RNG.  You only need to do this
        in the rare case that you need to guarantee that two RNGs have identical internal
        state.  Otherwise, disregard this method.  See stateEquals(other).
    */
    public void clearGaussian() { __haveNextNextGaussian = false; }

    public double nextGaussian()
        {
        if (__haveNextNextGaussian)
            {
            __haveNextNextGaussian = false;
            return __nextNextGaussian;
            } 
        else 
            {
            double v1, v2, s;
            do 
                { 
                int y;
                int z;
                int a;
                int b;
                    
                if (mti >= N)   // generate N words at one time
                    {
                    int kk;
                    final int[] mt = this.mt; // locals are slightly faster 
                    final int[] mag01 = this.mag01; // locals are slightly faster 
                    
                    for (kk = 0; kk < N - M; kk++)
                        {
                        y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                        }
                    for (; kk < N-1; kk++)
                        {
                        y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                        }
                    y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];
                    
                    mti = 0;
                    }
                
                y = mt[mti++];
                y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
                y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
                y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
                y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)
                
                if (mti >= N)   // generate N words at one time
                    {
                    int kk;
                    final int[] mt = this.mt; // locals are slightly faster 
                    final int[] mag01 = this.mag01; // locals are slightly faster 
                    
                    for (kk = 0; kk < N - M; kk++)
                        {
                        z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+M] ^ (z >>> 1) ^ mag01[z & 0x1];
                        }
                    for (; kk < N-1; kk++)
                        {
                        z = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+(M-N)] ^ (z >>> 1) ^ mag01[z & 0x1];
                        }
                    z = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N-1] = mt[M-1] ^ (z >>> 1) ^ mag01[z & 0x1];
                    
                    mti = 0;
                    }
                
                z = mt[mti++];
                z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
                z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
                z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
                z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)
                
                if (mti >= N)   // generate N words at one time
                    {
                    int kk;
                    final int[] mt = this.mt; // locals are slightly faster 
                    final int[] mag01 = this.mag01; // locals are slightly faster 
                    
                    for (kk = 0; kk < N - M; kk++)
                        {
                        a = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+M] ^ (a >>> 1) ^ mag01[a & 0x1];
                        }
                    for (; kk < N-1; kk++)
                        {
                        a = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+(M-N)] ^ (a >>> 1) ^ mag01[a & 0x1];
                        }
                    a = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N-1] = mt[M-1] ^ (a >>> 1) ^ mag01[a & 0x1];
                    
                    mti = 0;
                    }
                
                a = mt[mti++];
                a ^= a >>> 11;                          // TEMPERING_SHIFT_U(a)
                a ^= (a << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(a)
                a ^= (a << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(a)
                a ^= (a >>> 18);                        // TEMPERING_SHIFT_L(a)
                
                if (mti >= N)   // generate N words at one time
                    {
                    int kk;
                    final int[] mt = this.mt; // locals are slightly faster 
                    final int[] mag01 = this.mag01; // locals are slightly faster 
                    
                    for (kk = 0; kk < N - M; kk++)
                        {
                        b = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+M] ^ (b >>> 1) ^ mag01[b & 0x1];
                        }
                    for (; kk < N-1; kk++)
                        {
                        b = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                        mt[kk] = mt[kk+(M-N)] ^ (b >>> 1) ^ mag01[b & 0x1];
                        }
                    b = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                    mt[N-1] = mt[M-1] ^ (b >>> 1) ^ mag01[b & 0x1];
                    
                    mti = 0;
                    }
                
                b = mt[mti++];
                b ^= b >>> 11;                          // TEMPERING_SHIFT_U(b)
                b ^= (b << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(b)
                b ^= (b << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(b)
                b ^= (b >>> 18);                        // TEMPERING_SHIFT_L(b)
                
                /* derived from nextDouble documentation in jdk 1.2 docs, see top */
                v1 = 2 *
                    (((((long)(y >>> 6)) << 27) + (z >>> 5)) / (double)(1L << 53))
                    - 1;
                v2 = 2 * (((((long)(a >>> 6)) << 27) + (b >>> 5)) / (double)(1L << 53))
                    - 1;
                s = v1 * v1 + v2 * v2;
                } while (s >= 1 || s==0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s)/s);
            __nextNextGaussian = v2 * multiplier;
            __haveNextNextGaussian = true;
            return v1 * multiplier;
            }
        }

    /** Returns a random float in the half-open range from [0.0f,1.0f).  Thus 0.0f is a valid
        result but 1.0f is not. */
    public float nextFloat()
        {
        int y;
        
        if (mti >= N)   // generate N words at one time
            {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster 
            final int[] mag01 = this.mag01; // locals are slightly faster 
            
            for (kk = 0; kk < N - M; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            for (; kk < N-1; kk++)
                {
                y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                }
            y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];

            mti = 0;
            }
  
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        return (y >>> 8) / ((float)(1 << 24));
        }
		
    /** Returns a float in the range from 0.0f to 1.0f, possibly inclusive of 0.0f and 1.0f themselves.  Thus:

        <p><table border=0>
        <th><td>Expression<td>Interval
        <tr><td>nextFloat(false, false)<td>(0.0f, 1.0f)
        <tr><td>nextFloat(true, false)<td>[0.0f, 1.0f)
        <tr><td>nextFloat(false, true)<td>(0.0f, 1.0f]
        <tr><td>nextFloat(true, true)<td>[0.0f, 1.0f]
        </table>
        
        <p>This version preserves all possible random values in the float range.
    */
    public float nextFloat(boolean includeZero, boolean includeOne)
        {
        float d = 0.0f;
        do
            {
            d = nextFloat();                            // grab a value, initially from half-open [0.0f, 1.0f)
            if (includeOne && nextBoolean()) d += 1.0f; // if includeOne, with 1/2 probability, push to [1.0f, 2.0f)
            } 
        while ( (d > 1.0f) ||                           // everything above 1.0f is always invalid
            (!includeZero && d == 0.0f));           // if we're not including zero, 0.0f is invalid
        return d;
        }

    /** Returns an integer drawn uniformly from 0 to n-1.  Suffice it to say,
        n must be > 0, or an IllegalArgumentException is raised. */
    public int nextInt(int n)
        {
        if (n<=0)
            throw new IllegalArgumentException("n must be positive, got: " + n);
        
        if ((n & -n) == n)  // i.e., n is a power of 2
            {
            int y;
        
            if (mti >= N)   // generate N words at one time
                {
                int kk;
                final int[] mt = this.mt; // locals are slightly faster 
                final int[] mag01 = this.mag01; // locals are slightly faster 
                
                for (kk = 0; kk < N - M; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                for (; kk < N-1; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];
                
                mti = 0;
                }
            
            y = mt[mti++];
            y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)
            
            return (int)((n * (long) (y >>> 1) ) >> 31);
            }
        
        int bits, val;
        do 
            {
            int y;
            
            if (mti >= N)   // generate N words at one time
                {
                int kk;
                final int[] mt = this.mt; // locals are slightly faster 
                final int[] mag01 = this.mag01; // locals are slightly faster 
                
                for (kk = 0; kk < N - M; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+M] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                for (; kk < N-1; kk++)
                    {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk+1] & LOWER_MASK);
                    mt[kk] = mt[kk+(M-N)] ^ (y >>> 1) ^ mag01[y & 0x1];
                    }
                y = (mt[N-1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N-1] = mt[M-1] ^ (y >>> 1) ^ mag01[y & 0x1];
                
                mti = 0;
                }
            
            y = mt[mti++];
            y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
            y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
            y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
            y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)
        
            bits = (y >>> 1);
            val = bits % n;
            } while(bits - val + (n-1) < 0);
        return val;
        }
    /**
     * 
     * @param isAuto take random from system: true/false
     */
    public void setIsAuto(boolean isAuto) {
        this.isAuto = isAuto;
    }
    
     /**
     * finds a random value between 0 and 1
     *
     * @return a random real value between 0 an 1
     */
    public double random() {
        if (isAuto) {
            return Math.random();
        } else {
            return nextDouble();
        }
    }

    /**
     *
     * @param low lower bound
     * @param high upper bound
     * @return a random double between low and high
     */
    public double random(double low, double high) {
        if (isAuto) {
            return (double) (Math.random() * (high - low) + low);//take from system
        } else {
            return (double) (random() * (high - low) + low);//take from seed
        }
    }//random

    /**
     *
     * @param low lower bound
     * @param high upper bound
     * @return a random integer between low and high
     */
    public int random(int low, int high) {
        if (isAuto) {
            return (int) (Math.random() * (high - low) + low);//take from system
        } else {
            return (int) (random() * (high - low) + low);//take from seed
        }
    }//random 
    /**
     * 
     * @param range 0 to max
     * @return return number between 0 to max
     */
    public int random(int range) {
        if (isAuto) {
            return (int) (Math.floor(Math.random() * range));//take from system
        } else {
            return (int) (Math.floor(random() * range));//take from seed
        }
    }//random

    /**
     * Generating permutation within min and max (excluding max)
     *
     * @param min lower bound
     * @param max upper bound max -1
     * @return a vector of random numbers between min and max - 1
     */
    public int[] randomIntVector(int min, int max) {
        int a[] = new int[max];
        for (int i = 0; i < max; i++) {
            if (i == 0) {
                a[i] = random(min, max);
            } else {
                while (true) {
                    boolean flag = false;
                    int r = random(min, max);
                    for (int j = 0; j < i; j++) {
                        if (r == a[j]) {
                            flag = true;
                            break;
                        }//if	
                    }//for
                    if (!flag) {
                        a[i] = r;
                        break;
                    }//if   
                }//while				
            }//else 	
            //System.out.println(i + " - " + a[i] + " ");
        }//for
        return a;
    }//permutation 
    
	
    /**
     * Tests the code.
     */
    public static void main(String args[])
        { 
          int j;
		  byte b;
		  int max = 10;
		  int max2 = 22;
		  double mn = -2.0;
		  double mx = 2.0;
		  
          MersenneTwisterFast r;
		  final long SEED = 4357;
		  r = new MersenneTwisterFast(SEED);
		  for (j = 0; j < 1; j++)
          {
			System.out.println(r.nextBoolean() + " ");//Grab the first 1000 booleans
			System.out.println(r.nextBoolean((double)(j/999.0)) + " ");//Grab 1000 booleans of increasing probability using nextBoolean(double)
			System.out.println(r.nextBoolean((float)(j/999.0f)) + " ");//Grab 1000 booleans of increasing probability using nextBoolean(float)
			System.out.println((b = r.nextByte()) + " ");//Grab the first 100 bytes -- must be same as nextBytes
			System.out.println(r.nextShort() + " ");//Grab the first 1000 shorts
			System.out.println(r.nextInt() + " ");//Grab the first 1000 ints
			System.out.println(r.nextInt(max) + " ");//Grab the first 1000 ints of different sizes
			System.out.println(r.nextLong() + " ");//Grab the first 1000 longs
			System.out.println(r.nextLong(max2) + " ");//Grab the first 1000 longs of different sizes
			System.out.println(r.nextFloat() + " ");//Grab the first 1000 floats
			System.out.println(r.nextDouble() + " ");//Grab the first 1000 doubles
			System.out.println(((mx-mn)*r.nextDouble()+ mn) + " ");//Grab the first 1000 doubles between a and b
			System.out.println(r.nextGaussian() + " ");//Grab the first 1000 gaussian doubles
		  }	
		  
		  //byte[] bytes = new byte[100];
          //r.nextBytes(bytes);//Grab the first 1000 bytes using nextBytes and stored in variable bytes
        
        }


    }
