package org.donntu.knt.mksit.lab2.v3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Optional;

public class LZ77_v3_my {
    private final int MAX_WINDOW_SIZE;

    public LZ77_v3_my(int maxWindowSize) {
        this.MAX_WINDOW_SIZE = maxWindowSize;
    }

    public void compress(String inputFileName, String outputFileName) {
        try (
                BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFileName));
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName))
        ) {
            Optional<String> textOptional = bufferedReader.lines().reduce((s, s2) -> s + '\n' +  s2);
            if(textOptional.isPresent()){
                String text = textOptional.get();
                FlyingWindow window = new FlyingWindow();
                StringBuffer mutableText = new StringBuffer(text);
                mutableText.deleteCharAt(0);
                while (moveWindow(window, text)) {
                    Match match = checkMatches(window, mutableText);
                    if(match != null) {
                        bufferedWriter.write('<' + match.getOffset() + ';' + match.getLength() + '>');
                        mutableText.delete(0, match.getLength());
                    } else {
                        bufferedWriter.write(mutableText.charAt(0));
                        mutableText.deleteCharAt(0);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean moveWindow(FlyingWindow window, String text) {
        try {
            window.getWindow().append(
                    text.charAt(
                            window.getStartWindowPosition() + window.getWindow().length()
                    )
            );
            if (window.getWindow().length() > MAX_WINDOW_SIZE) {
                window.getWindow().deleteCharAt(0);
            }
            return true;
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }


    //TODO:решить как вытягивать совпадения, если цикл окончен
    private Match checkMatches(FlyingWindow window, StringBuffer text) {
        StringBuilder matchString = new StringBuilder();
        char[] chars = window.getWindow().toString().toCharArray();
        int currentTextPosition = 0;
        Match match = null;
        for (int i = 0; i < chars.length; i++) {
            final char aChar = chars[i];
            if (aChar == text.charAt(currentTextPosition)) {
                matchString.append(aChar);
                currentTextPosition++;
            } else if (currentTextPosition != 0) {
                match = new Match(
                        window.getStartWindowPosition() + i - currentTextPosition,
                        matchString.length()
                );
                break;
            }
        }
        return null;
    }

}
