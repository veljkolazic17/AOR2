package rs.ac.bg.etf.aor2;




public class MojTest {

    static String toBinary(int x, int len){
        if(len > 0){
            return String.format("%"+len+"s",Integer.toBinaryString(x)).replaceAll(" ","0");
        }
        return null;
    }
    static int get_result(int LRUCnt, int associativity){
        int first_bit = (int) (Math.random()%2);
        String[] bits;
        int iter_index = 0;
        switch (associativity){
            case 4 ->{
//                LRUCnt |= first_bit<<2;
                bits = toBinary(LRUCnt,3).split("");
                for(int i = 0;i<1;i++){
                    if(bits[iter_index].equals("0")){
                        iter_index = iter_index*2 + 1;
                    }
                    else {
                        iter_index = iter_index*2 + 2;
                    }
                }
                String iter_bit = bits[iter_index];
                iter_index-=1;
                if(iter_bit.equals("0")){
                    return (Math.abs(iter_index-1)*2)+1;
                }
                else {
                    return (Math.abs(iter_index-1)*2);
                }


            }
            case 8 ->{
//                LRUCnt |= first_bit<<6;
                bits = toBinary(LRUCnt,7).split("");
                for(int i = 0;i<2;i++){
                    if(bits[iter_index].equals("0")){
                        iter_index = iter_index*2 + 1;
                    }
                    else {
                        iter_index = iter_index*2 + 2;
                    }
                }
                String iter_bit = bits[iter_index];
                iter_index-=3;
                if(iter_bit.equals("0")){
                    return (Math.abs(iter_index-3)*2)+1;
                }
                else {
                    return (Math.abs(iter_index-3)*2);
                }

            }
            case 16 ->{
                LRUCnt |= first_bit<<14;
                bits = toBinary(LRUCnt,15).split("");

            }
            case 32 ->{
                LRUCnt |= first_bit<<30;
                bits = toBinary(LRUCnt,31).split("");

            }
        }

        return -1;
    }

    public static void main(String[] args) {
        //  4 5 6 7
        int LRUCNTS[] = new int[]{0b1000010,0b1000011,0b1001010,0b1100010};

        for(int i = 0;i<LRUCNTS.length;i++){
            System.out.println(get_result(LRUCNTS[i],8));
        }
    }

}
