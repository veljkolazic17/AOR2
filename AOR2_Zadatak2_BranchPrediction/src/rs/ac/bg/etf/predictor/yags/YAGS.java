package rs.ac.bg.etf.predictor.yags;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class YAGS implements Predictor {

    BHR bhr;
    int addres_mask;
    int selector_mask;
    InternalBimodal internalBimodal;
    InternalGshare internalGshareTaken;
    InternalGshare internalGshareNotTaken;
    Flags FLAG;
    enum Flags {
        GSHARETAKEN,
        GSHARENOTTAKEN,
        BIMODAL
    }

    public static class InternalBimodal implements Predictor{

        BHR bhr;
        int addres_mask;
        int selector_mask;

        Automaton[][] phts;
        int selector[];

        public InternalBimodal(BHR bhr, int addres_mask, int selector_mask, Automaton.AutomatonType type){
            this.addres_mask = addres_mask;
            this.selector_mask = selector_mask;
            this.bhr = bhr;

            phts = new Automaton[2][];
            phts[0] = Automaton.instanceArray(type,this.addres_mask+1);
            phts[1] = Automaton.instanceArray(type,this.addres_mask+1);
            selector = new int[selector_mask+1];
        }


        @Override
        public boolean predict(Instruction branch) {
            int entry_pht = (int) ((bhr.getValue() ^ branch.getAddress())&addres_mask);
            int entr_selector = (int) (branch.getAddress()&selector_mask);
            int selector_out = selector[entr_selector]<0?0:1;
            return phts[selector_out][entry_pht].predict();
        }

        @Override
        public void update(Instruction branch) {
            int entry_pht = (int) ((bhr.getValue() ^ branch.getAddress())&addres_mask);
            int entry_selector = (int) (branch.getAddress()&selector_mask);
            int selector_out = selector[entry_selector]<0?0:1;
            boolean outcome = branch.isTaken();

            phts[selector_out][entry_pht].updateAutomaton(outcome);

            if(outcome) {
                if(selector[selector_out] < 2) selector[selector_out]++;
            } else {
                if(selector[selector_out] > -1) selector[selector_out]--;
            }
        }
    }
    public static class InternalGshare implements Predictor{
        BHR bhr;
        int address_mask;
        Automaton[] pht;
        long[] tags;
        public InternalGshare(BHR bhr, int address_mask, Automaton.AutomatonType type){
            this.bhr = bhr;
            this.address_mask = address_mask;
            pht = Automaton.instanceArray(type,address_mask+1);
            tags = new long[address_mask+1];
        }

        @Override
        public boolean predict(Instruction branch) {
            int entry_pht = (int)(branch.getAddress()^bhr.getValue())&address_mask;
            return pht[entry_pht].predict();
        }

        @Override
        public void update(Instruction branch) {
            int entry_pht = (int)(branch.getAddress()^bhr.getValue())&address_mask;
            pht[entry_pht].updateAutomaton(branch.isTaken());
        }

        public void update_tag(Instruction branch){
            int entry_tag = (int)(branch.getAddress()^bhr.getValue())&address_mask;
            tags[entry_tag] = branch.getAddress()&address_mask;
        }

        public long get_tag(Instruction branch){
            int entry_tag = (int)(branch.getAddress()^bhr.getValue())&address_mask;
            return tags[entry_tag];
        }
    }

    public YAGS(int bhr_size, int addres_size, int selector_size, Automaton.AutomatonType type){

        bhr = new BHR(bhr_size);
        addres_mask = (1<<addres_size)-1;
        selector_mask = (1<<selector_size)-1;

        internalBimodal = new InternalBimodal(bhr,addres_mask,selector_mask,type);
        internalGshareTaken = new InternalGshare(bhr,addres_mask,type);
        internalGshareNotTaken = new InternalGshare(bhr,addres_mask,type);
    }


    @Override
    public boolean predict(Instruction branch) {

        boolean bimodal_outcome = internalBimodal.predict(branch);
        if(bimodal_outcome){
            if(internalGshareNotTaken.get_tag(branch) == (branch.getAddress()&addres_mask)){
                FLAG = Flags.GSHARENOTTAKEN;
                return internalGshareNotTaken.predict(branch);
            }else {
                FLAG = Flags.BIMODAL;
                return true;
            }
        }else {
            if(internalGshareTaken.get_tag(branch) == (branch.getAddress()&addres_mask)){
                FLAG = Flags.GSHARETAKEN;
                return internalGshareTaken.predict(branch);
            }else {
                FLAG = Flags.BIMODAL;
                return false;
            }
        }
    }

    @Override
    public void update(Instruction branch) {

        if(FLAG == Flags.GSHARETAKEN){
            internalGshareTaken.update_tag(branch);
            internalGshareTaken.update(branch);
        }
        else if(FLAG == Flags.GSHARENOTTAKEN){
            internalGshareNotTaken.update_tag(branch);
            internalGshareNotTaken.update(branch);
        }
        else if(FLAG == Flags.BIMODAL) {
            if(!internalBimodal.predict(branch) && branch.isTaken()){
                internalGshareTaken.update_tag(branch);
                internalGshareTaken.update(branch);
            }
            else if(internalBimodal.predict(branch) && !branch.isTaken()){
                internalGshareNotTaken.update_tag(branch);
                internalGshareNotTaken.update(branch);
            }
        }
        internalBimodal.update(branch);
        bhr.insertOutcome(branch.isTaken());
    }
}