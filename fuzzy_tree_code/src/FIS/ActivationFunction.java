package FIS;

public class ActivationFunction {

    /**
     * Returns the activation function value
     *
     * @param a function parameter
     * @param b function parameter
     * @param actFun type of activation function
     * @param netn inner-product of weights of input signals
     * @return activation output value
     */
    public double getOutput(double a, double b, int actFun, double netn) {
        double ret = 0;
        switch (actFun) {
            case 1:
                ret = Fa(a, b, netn);//"gaussian"
                break;
            case 2:
                ret = Fb(netn);//tanh
                break;
            case 3:
                ret = Fc(netn);//fermi
                break;
            case 4:
                ret = Fd(a, b, netn);//linear fermi
                break;
            case 5:
                ret = Fe(a, b, netn);//linear tanh
                break;
            case 6:
                ret = Ff(a, netn);//uni-Sigmod 
                break;
            case 7:
                ret = Fg(a, b, netn);//bi sigmoid
                break;
        }
        return ret;
    }

    /**
     * Gaussian function a = mu and b = sigma
     *
     * @param a function parameter
     * @param b function parameter
     * @param netn inner-product of weights of input signals
     * @return activation output value
     */
    private double Fa(double a, double b, double netn) {
        netn = ((netn - a) / b);
        netn = netn * netn;
        netn = -netn;
        return Math.pow(2.71828182845904523536, netn);
    }

    /**
     * tangent hyperbolic
     *
     * @param netn inner-product of weights of input signals
     * @return activation output value
     */
    private double Fb(double netn) {
        double exp1 = Math.pow(2.71828182845904523536, netn);
        double exp2 = Math.pow(2.71828182845904523536, -netn);
        return (exp1 - exp2) / (exp1 + exp2);
    }

    /**
     * Fermi
     *
     * @param netn inner-product of weights of input signals
     * @return activation output value
     */
    private double Fc(double netn) {
        double exp = Math.pow(2.71828182845904523536, -netn);
        return 1 / (1 + exp);
    }

    /**
     * linear Fermi
     *
     * @param a function parameter
     * @param b function parameter
     * @param netn inner-product of weights of input signals
     * @return activation output value
     */
    private double Fd(double a, double b, double netn) {
        return a * Fc(netn) + b;
    }

    /**
     * linear tangent-hyperbolic
     *
     * @param a function parameter
     * @param b function parameter
     * @param netn inner-product of weights of input signals
     * @return activation output value
     */
    private double Fe(double a, double b, double netn) {
        return a * Fb(netn) + b;
    }

    private double Ff(double a, double netn) {
        a = Math.abs(a);
        return ((2.0 * a) / (1.0 + Math.exp(- 2 * a * netn)));
    }

    private double Fg(double a, double b, double netn) {
        a = Math.abs(a);
        return (1.0 - Math.exp(- 2 * a * netn)) / (a * (1.0 + Math.exp(- 2 * a * netn)));
    }

}//end class Activation Function
