package de.js_labs.lateinprima;

import android.util.Log;

import static de.js_labs.lateinprima.MainActivity.LOG_TAG;

public class Lektion {
    public int id;
    public String data;
    public Vokablel[] vokablels;

    public Lektion(int id){
        this.id = id;
    }

    public void setData(String rawData){
        this.data = rawData.replaceAll("NEWLINE","\n");
    }

    public void setVoc(String vocData){
        String[] allVoc = vocData.split("NEWVOC");
        int selector = 1;
        int counter = 0;
        for(String voc : allVoc){
            if (selector == 1){
                selector = 2;
            }else{
                selector = 1;
                counter++;
            }
        }
        vokablels = new Vokablel[counter];
        for(int i = 0; i < vokablels.length; i++){
            vokablels[i] = new Vokablel();
        }
        selector = 1;
        counter = 0;
        if(vokablels.length > 0){
            for(int i = 0; i < allVoc.length; i++){
                if (selector == 1){
                    String[] currentLatin = allVoc[i].split("FORMS");
                    vokablels[counter].latein = currentLatin[0];
                    if(currentLatin.length > 1){
                        vokablels[counter].formen = currentLatin[1];
                    }else{
                        vokablels[counter].formen = "";
                    }
                    selector = 2;
                }else {
                    vokablels[counter].deutsch = allVoc[i];
                    selector = 1;
                    counter++;
                }
            }
        }
    }
}
