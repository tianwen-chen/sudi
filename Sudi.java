import jdk.swing.interop.SwingInterOpUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Sudi {
    //instance variables
    private HashMap<String, HashMap<String,Double>> trainingData;
    private HashMap<String,HashMap<String, Double>> observation;
    private double unseenWordPenalty = -100.0;
    public boolean unseen = false;

    /**
     * constructor
     */
    public Sudi(){
        trainingData = new HashMap<>();
        observation = new HashMap<>();
    }

    /**
     * setter for training data and observation, for viterbi test
     */
    public void setTrainingData(HashMap<String, HashMap<String, Double>> inputData){
        trainingData = inputData;
    }

    public void setObservation(HashMap<String, HashMap<String, Double>> inputObser){
        observation = inputObser;
    }

    public HashMap<String,HashMap<String, Double>> getObservation(){return observation;}

    public HashMap<String, HashMap<String, Double>> getTrainingData(){return trainingData;}

    /**
     * V algorithm
     * @param: given observation sentence, which is an array of strings(words)
     * @return: optimum route
     */
    public ArrayList<String> Viterbi(ArrayList<String> observ){
        HashSet<String> currStates = new HashSet<>(); //** currState = start
        currStates.add("#");
        HashMap<String, Double> currScore = new HashMap<>(); //** currScore: #-->0
        currScore.put("#",0.0);
        ArrayList<HashMap<String, String>> backTrack = new ArrayList<>();
        String lastWord = "";
        String theLastWord = "";
        unseen = false;
        //loop over each word, until at index i-1
        for(int i=0;i<observ.size();i++){
            //initiate next states & scores
            HashSet<String> nextStates = new HashSet<>();
            HashMap<String, Double> nextScore = new HashMap<>();   //keep all the possible scores
            //loop through all the current states
            for(String state:currStates){
                //loop through all its out neighbors
                for(String neibr:trainingData.get(state).keySet()){
                    nextStates.add(neibr);  //add neighbour to next state
                    /**check transition score, observation score, current score, if not there, put an arbitrary small score*/
                    double observationScore, transitionScore, currStateScore;
                    //observationScore = observation.get(neibr).getOrDefault(observ.get(i), unseenWordPenalty);
                    if(!observation.get(neibr).containsKey(observ.get(i))){
                        observationScore = unseenWordPenalty;
                        unseen = true;
                    }
                    else{
                        observationScore = observation.get(neibr).get(observ.get(i));
                    }
                    transitionScore = trainingData.get(state).get(neibr);
                    //System.out.println(currScore.keySet());
                    currStateScore = currScore.get(state);
                    double score = transitionScore + currStateScore + observationScore;
                    //update currScore --> if it is larger than the one already there, or it wasn't there
                    if(!nextScore.containsKey(neibr)||nextScore.get(neibr)<score){
                        nextScore.put(neibr,score);
                        //update backtrack: if not there, add a map; if there, add an entry to the map
                        if(backTrack.size()<i+1){
                            backTrack.add(i,new HashMap<>());
                        }
                        backTrack.get(i).put(neibr,state); //put: neighbour --> state
                    }
                }
            }
            currStates = nextStates;
            currScore = nextScore;
        }
        //when at the last word, decide which tag to take (find the highest score)
        double max = -10000.0; //arbitrarily small number
        String maxKey = "";
        for(String k: currScore.keySet()){
            if(currScore.get(k)>max){
                maxKey = k;
                max = currScore.get(k);
            }
        }
        theLastWord = maxKey;
        //get the route
        ArrayList<String> res = new ArrayList<>();
        Stack<String> temp = new Stack<>();
        temp.add(theLastWord);
        for(int i=backTrack.size()-1;i>0;i--){
            String nextWord = backTrack.get(i).get(theLastWord);
            temp.add(nextWord);
            theLastWord = nextWord;
        }
        while(!temp.empty()){
            res.add(temp.pop());
        }
        return res;
    }

    /**
     * training method
     * @param: sentence file name, tag file name
     */
    public void train(String sentence, String tag) throws IOException {
        BufferedReader inSentence, inTag;
        try{
            inSentence = new BufferedReader(new FileReader(sentence));
            inTag = new BufferedReader(new FileReader(tag));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            //into lower case!
            String sline = inSentence.readLine().toLowerCase();
            String tline = inTag.readLine();        //read two files together
            //put # (start state) into training map
            trainingData.put("#",new HashMap<>());
            //keep track of totals: two maps
            HashMap<String, Integer> totalState = new HashMap<>();
            HashMap<String, Integer> totalObserv = new HashMap<>();
            totalState.put("#",0);
            while (sline!=null){
                String[] sInfo = sline.split(" ");
                String[] tInfo = tline.split(" ");
                for(int i=0;i<tInfo.length;i++){
                    String currTag = tInfo[i];
                    String currWord = sInfo[i];
                    String lastTag;
                    if(!totalState.containsKey(currTag)) totalState.put(currTag,0);
                    if(!totalObserv.containsKey(currTag)) totalObserv.put(currTag,0);
                    //put into POS map:
                    //1. if it is the first POS of the sentence, add to #, increment count, add it to outer map key
                    if(i==0) lastTag = "#";
                    //2. otherwise, after adding to the last POS's nested map, add it to the outer map entry, increment count
                    else lastTag = tInfo[i-1];
                    //initiate inner map key
                    if(!trainingData.get(lastTag).containsKey(currTag))trainingData.get(lastTag).put(currTag,0.0);
                    double updateCountTag = trainingData.get(lastTag).get(currTag)+1;
                    trainingData.get(lastTag).put(currTag,updateCountTag);
                    if(!trainingData.containsKey(currTag))trainingData.put(currTag,new HashMap<>());
                    //update total map
                    totalState.put(lastTag,totalState.get(lastTag)+1);

                    //put into observation map:
                    //1. add current POS to the map key
                    if(!observation.containsKey(currTag)){
                        observation.put(currTag,new HashMap<>());   //initiate outer map key
                    }
                    //2. add observed word to the corresponding POS, increment count
                    if(!observation.get(currTag).containsKey(currWord)){
                        observation.get(currTag).put(currWord,0.0); //initiate inner map key
                    }
                    double newCount = observation.get(currTag).get(currWord)+1;
                    observation.get(currTag).put(currWord,newCount);
                    //update total map
                    totalObserv.put(currTag,totalObserv.get(currTag)+1);
                }
                sline = inSentence.readLine();
                tline = inTag.readLine();
            }
            //System.out.println(observation);
            //System.out.println(trainingData);
            //after adding all cases to training data and observation map, calculate the probability and convert to log
            //go over each outer entry of each map, get each inner entry
            for(String pos1:trainingData.keySet()){
                for(String pos2:trainingData.get(pos1).keySet()){
                    double curr = trainingData.get(pos1).get(pos2);
                    int sum = totalState.get(pos1);
                    curr = Math.log(curr/sum);
                    trainingData.get(pos1).put(pos2,curr);
                }
            }
            for(String p:observation.keySet()){
                for(String w:observation.get(p).keySet()){
                    double temp = observation.get(p).get(w);
                    int sum = totalObserv.get(p);
                    temp = Math.log(temp/sum);
                    observation.get(p).put(w,temp);
                }
            }
            //System.out.println(trainingData);
            //System.out.println(observation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            inSentence.close();
            inTag.close();
        }
    }
}
