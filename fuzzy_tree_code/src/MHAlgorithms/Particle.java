package MHAlgorithms;

public class Particle {

    public int index;
    public double[] m_position; // equivalent to x-Values and/or solution    
    public double[] m_velocity;
    public double m_fitness = 2.0;

    public double[] m_bestPosition; // best position found so far by this Particle
    public double m_bestFitness;

    //public  Particle(){}
    public Particle(double[] m_position, double m_fitness, double[] m_velocity, double[] m_bestPosition, double m_bestFitness) {
        this.m_position = m_position;
        this.m_fitness = m_fitness;
        this.m_velocity = m_velocity;
        this.m_bestPosition = m_bestPosition;
        this.m_bestFitness = m_bestFitness;
    }
}//end Particle

