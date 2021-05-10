package com.stego_api.stego;

import java.util.ArrayList;
/**
 * This class controls the placing of text into the pixels and the extraction of text from the pixels.
 *
 */
public class Stego{
    public static void textIntoMap(String message, String fileContent){
        Integer[][] map = new Integer[12][12];
        Integer[] msg= new Integer[10];
        //p.reset();


        boolean ok=false;
        boolean min=false;
        boolean last=false;
        int bit=0;
        int character=0;
        String str="";

        for(int r=0;r<map .length;r++){
            for(int c=0;c<map [0].length;c++){
                if(character>msg.length-1){

                    return;
                }//if the message is ended

                if(ok==false){//if we are not in the middle of a section
                    if(c<map [0].length-3&&r>map .length-1){//if there is no more space for the message
                        //do nothing
                    }


                    //if the next 2 pixels are the same, set ok to true and this pixel to 0
                    else if(c<map [0].length-2){//if not at the end of the row
                        if(map [r][c].intValue()==map [r][c+1].intValue()){//test next pixel
                            if(map [r][c].intValue()==map [r][c+2].intValue()){//test pixel after next

                                ok=true;
                                min=true;//get ready to denote the minimum
                                //this will work because i can modify even pixels that are not the same value to be null
                            }
                        }
                    }

                    else if(r>map .length-2){//do nothing if this is at the end of the row and cannot go down any further
                    }

                    else if(c==map [0].length-2){//if there is 1 more space left
                        if(map [r][c].intValue()==map [r][c+1].intValue()){//test next pixel in row
                            if(map [r][c].intValue()==map [r+1][0].intValue()){//check pixel at next row, 0

                                ok=true;
                                min=true;
                            }
                        }
                    }

                    else if(c==map [0].length-1){//if at the end of the row
                        if(map [r][c].intValue()==map [r+1][0].intValue()){//check pixel at next row, 0
                            if(map [r][c].intValue()==map [r+1][1].intValue()){//check pixel at next row, 1

                                ok=true;
                                min=true;
                            }
                        }
                    }
                }


                else{//if ok==true, or rather, if we are in the middle of a section
                    if(last){//if we already modified the last same color pixel, end this

                        ok=false;
                        last=false;
                    }

                    //test next pixel to see if this section has to end
                    else if(c<map [0].length-1){//if there is at least 1 more space in this row
                        if(map [r][c].intValue()!=map [r][c+1].intValue()){//test next pixel
                            last=true;
                        }
                    }

                    else if(r==map .length-1){//if at the end of the image

                        ok=false;
                    }

                    else{//if we need to check next row-only option left
                        if(map [r][c].intValue()!=map [r+1][0].intValue()){
                            last=true;
                        }
                    }

                    if(min){//if we are on the minimum, leave it as is and set min to false
                        min=false;
                    }

                    else if(ok==true){//if the section continues, we can begin putting text bytecode into the pixels
                        str=Integer.toBinaryString(msg[character].intValue());//get the bytecode of the character we are on
                        while(str.length()<8){//make sure the string is the right length
                            str="0"+str;
                        }
                        str=str.substring(bit,bit+1);//now find the bit we are on
                        //after this, modify the original pixel according to the bit we find
                        if(str.equals("0"));//leave it alone
                        if(str.equals("1")){
//                            p.editPix(r,c,pixels[r][c].intValue()+1);
                        }

                        //at this point, increment the progress of the message
                        if(bit<7)
                            bit++;
                        else{
                            bit=0;
                            character++;
                        }
                    }
                }
            }
        }
    }

    private static ArrayList<Integer> convertToBaseTen(ArrayList<Integer> list)
    {
        ArrayList<Integer> convertedList = new ArrayList<Integer>();

        for(int i = 0; i < list.size()-7; i+=8)
        {
            int number = 0;
            for(int k = 7; k>=0; k--)
            {
                number += list.get(i+7-k)*Math.pow(2,k);
                //System.out.print(list.get(i+7-k));//debug
            }
            convertedList.add(number);
            //System.out.println("->"+number+"->"+(char)number);//debug
        }
        return convertedList;
    }
}