package rs.ac.bg.etf.predictor.twoLevel;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class TwoLevel implements Predictor {

    BHR bhr;
    Automaton[] pht;

    public TwoLevel(int BHRsize, Automaton.AutomatonType type) {
        bhr = new BHR(BHRsize);
        pht = Automaton.instanceArray(type, 1 << BHRsize);
    }

    @Override
    public boolean predict(Instruction branch) {
        return pht[bhr.getValue()].predict();
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome = branch.isTaken();
        pht[bhr.getValue()].updateAutomaton(outcome);
        bhr.insertOutcome(outcome);
    }
}
