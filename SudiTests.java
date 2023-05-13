import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SudiTests {
    public static void main(String[] args) throws IOException {

        /**
        //test for viterbi, from pd hmm
        //poc map
        HashMap<String, HashMap<String, Double>> poc = new HashMap<>();
        poc.put("#",new HashMap<>());
        poc.put("NP", new HashMap<>());
        poc.put("CNJ", new HashMap<>());
        poc.put("N", new HashMap<>());
        poc.put("V", new HashMap<>());
        poc.get("#").put("NP",3.0);
        poc.get("#").put("N",7.0);
        poc.get("NP").put("CNJ",2.0);
        poc.get("NP").put("V",8.0);
        poc.get("N").put("CNJ",2.0);
        poc.get("N").put("V",8.0);
        poc.get("CNJ").put("NP",2.0);
        poc.get("CNJ").put("N",4.0);
        poc.get("CNJ").put("V",4.0);
        poc.get("V").put("CNJ",2.0);
        poc.get("V").put("NP",4.0);
        poc.get("V").put("N",4.0);
        //observation map
        HashMap<String,HashMap<String,Double>> ob = new HashMap<>();
        ob.put("NP", new HashMap<>());
        ob.put("CNJ", new HashMap<>());
        ob.put("N", new HashMap<>());
        ob.put("V", new HashMap<>());
        ob.get("NP").put("chase",10.0);
        ob.get("CNJ").put("and",10.0);
        ob.get("N").put("cat",4.0);
        ob.get("N").put("dog",4.0);
        ob.get("N").put("watch",2.0);
        ob.get("V").put("get",1.0);
        ob.get("V").put("chase",3.0);
        ob.get("V").put("watch",6.0);
        //test viterbi
        Sudi testV = new Sudi();
        testV.setObservation(ob);
        testV.setTrainingData(poc);
        ArrayList<String> inputOb = new ArrayList<>();
        inputOb.add("chase");
        inputOb.add("watch");
        inputOb.add("dog");
        inputOb.add("chase");
        inputOb.add("watch");
        System.out.println(testV.Viterbi(inputOb)); */

        //console-based test method to give the tags from an input line
        //String sentenceIn = "texts/simple-train-sentences.txt";
        //String tagIn = "texts/simple-train-tags.txt";
        String sentenceIn = "texts/brown-train-sentences.txt";
        String tagIn = "texts/brown-train-tags.txt";
        Sudi testT = new Sudi();
        testT.train(sentenceIn,tagIn);

        /**System.out.println("Enter sentence: ");
        Scanner sc = new Scanner(System.in);
        String[] s = sc.nextLine().toLowerCase().split(" ");
        ArrayList<String> inputS = new ArrayList<>();
        inputS.addAll(Arrays.asList(s));
        System.out.println(testT.Viterbi(inputS));*/

        //file-based training
        //read the test sentence and test tag file line by line,
        //put the sentence into Viterbi, compare the output with the line from tag file
        BufferedReader readS, readT;
        try{
            //readS = new BufferedReader(new FileReader("texts/simple-test-sentences.txt"));
            //readT = new BufferedReader(new FileReader("texts/simple-test-tags.txt"));
            readS = new BufferedReader(new FileReader("texts/brown-test-sentences.txt"));
            readT = new BufferedReader(new FileReader("texts/brown-test-tags.txt"));
        }catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }try{
            String inS = readS.readLine();
            String inT = readT.readLine();
            int correct = 0;
            int wrong = 0;
            while (inS!=null){
                String[] lineS = inS.toLowerCase().split(" ");
                String[] lineT = inT.split(" ");
                //convert lineS to array list of string and pass it to viterbi
                ArrayList<String> inputSentence = new ArrayList<>();
                ArrayList<String> correctTag = new ArrayList<>();
                inputSentence.addAll(Arrays.asList(lineS));
                correctTag.addAll(Arrays.asList(lineT));
                ArrayList<String> resultTag = testT.Viterbi(inputSentence);
                for(int i=0;i<resultTag.size();i++){
                    if(Objects.equals(resultTag.get(i), correctTag.get(i))) correct += 1;
                    else{
                        wrong += 1;
                        /**if (testT.unseen){  //if has unseen word, output sentence
                            System.out.println(inS);
                            System.out.println(resultTag);
                            System.out.println(correctTag);
                        }*/
                    }
                }
                inS = readS.readLine();
                inT = readT.readLine();
            }
            System.out.println("Got correct: " + correct);
            System.out.println("Got wrong: " + wrong);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            readS.close();
            readT.close();
        }
    }
}
