package rs.ac.bg.etf.predictor.tage;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.automaton.Automaton.AutomatonType;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class TAGE implements Predictor{


    // Constants used in TAGE algorithm
    static final double alpha = 2;
    static final int L1 = 2;
    static final int T_count = 5;


    BHR bhr;
    int addres_mask;
    int selector_mask;

    int max_A = Integer.MIN_VALUE;
    int max_B = Integer.MIN_VALUE;

    int age_counter = 0;
    int update_state;

    boolean max_A_prediction;
    boolean max_B_prediction;

    // TAGE entry decomposed
    Automaton counters[][] = new Automaton[T_count][];
    int tags[][] = new int[T_count][];
    int u[][] = new int[T_count][];

    private double Li(int i){
        return Math.pow(alpha, i-1)*L1 + 0.5;
    }

    public TAGE(int bhr_size, int addres_size, Automaton.AutomatonType type){
        bhr = new BHR(bhr_size);
        addres_mask = (1<<addres_size)-1;

        if(type == AutomatonType.ONEBIT){
            update_state = 1;
        } else{
            update_state = 2;
        }

        for(int i = 0; i<T_count; i++){
            counters[i] = Automaton.instanceArray(type, addres_mask+1);
            if(i != 0){
                tags[i] = new int[addres_mask+1];
                u[i] = new int[addres_mask+1];

                int Li_ = (int)Li(i);

                for(int j = 0; j < addres_mask+1; j++){
                    tags[i][j] = j & ((1 << Li_) - 1);
                    u[i][j] = 0;
                }
            }
            
        }
    }

    private long hash_addres(long addres, int i){
        return (addres ^ bhr.getValue()) & ((1 << (int)Li(i)) - 1);
    }

    @Override
    public boolean predict(Instruction branch) {
        long addres = branch.getAddress();
        ++age_counter;

        max_A = Integer.MIN_VALUE;
        max_B = Integer.MIN_VALUE;

        for(int i = T_count-1; i > 0; i--){
            int haddres = (int)hash_addres(addres, i);
            
            if(tags[i][haddres] == haddres){
                if(i > max_A) {
                    max_A = i;
                    max_A_prediction = counters[i][haddres].predict();
                } else if (i > max_B) {
                    max_B = i;
                    max_B_prediction = counters[i][haddres].predict();
                }
            }
        }
        if(max_A == Integer.MIN_VALUE){
            max_A = 0;
            max_A_prediction = counters[max_A][(int)addres & addres_mask].predict();
        }
        if(max_B == Integer.MIN_VALUE){
            max_B = 0;
            max_B_prediction = counters[max_B][(int)addres & addres_mask].predict();
        }
        return max_A_prediction;
    }

    void reset_useful_counters(){
        age_counter = 0;
        for(int i = 1; i<T_count;i++){
            for(int j = 0; j < addres_mask+1; j++){
                u[i][j] = 0;
            }
        }
    }

    @Override
    public void update(Instruction branch) {
        boolean taken = branch.isTaken();
        long addres = branch.getAddress();


        int haddres_A = (max_A != Integer.MIN_VALUE && max_A != 0)?(int)hash_addres(addres, max_A):addres_mask & addres_mask;
        
        // Update counters
        counters[max_A][haddres_A].updateAutomaton(taken);
        if(max_B != Integer.MIN_VALUE){
            counters[max_B][haddres_A].updateAutomaton(taken);
        }

        
        /*
         * Increment if prediction is same as alt prediction,
         * otherwise decrement every u in Tk
        */
        if(max_A_prediction != max_B_prediction){
            if(max_A_prediction == taken && max_A != 0){
                u[max_A][haddres_A]++;
                if(u[max_A][haddres_A] > 3){
                    u[max_A][haddres_A] = 3;
                }
            }
        }
        else{
            u[max_A][haddres_A]--;
            if(u[max_A][haddres_A] < 0){
                u[max_A][haddres_A] = 0;
            }
        }

        if(age_counter == 256000)
            reset_useful_counters();

        if(max_A_prediction != taken && max_A < 4){
            boolean found_alocated = false;
            for(int i = max_A + 1; i < T_count; i++){
                for(int j = 0; j < addres_mask+1; j++){
                    if(u[i][j] == 0 && found_alocated == false){
                        counters[i][j].setState(update_state);
                        found_alocated = true;
                    }
                    else{
                        if(u[i][j] > 0)u[i][j]--;
                    }
                }
            }
        }
        bhr.insertOutcome(taken);
    }
}
