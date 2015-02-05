package g419.corpus.structure;

import java.util.ArrayList;

/**
 * Created by michal on 1/8/15.
 */
public class WrappedToken extends Token{
    ArrayList<Token> oldTokens;
    Sentence oldSentence;

    public WrappedToken(String orth, Tag firstTag, TokenAttributeIndex attrIdx) {
        super(orth, firstTag, attrIdx);
        oldTokens = new ArrayList<Token>();
    }

    public void addToken(Token t){
        oldTokens.add(t);
    }

    public void setOldSentence(Sentence s){
        oldSentence = s;
    }
}