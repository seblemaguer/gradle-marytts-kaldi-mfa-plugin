package de.dfki.mary.voicebuilding.io

import marytts.data.SupportedSequenceType
import marytts.data.Utterance;
import marytts.data.Sequence;
import marytts.data.item.linguistic.Word;
import marytts.io.MaryIOException;
import marytts.io.serializer.Serializer;

/**
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le
 *         Maguer</a>
 */
public class MFASerializer implements Serializer {

    /**
     * Constructor
     *
     */
    public UtteranceSerializer() {
    }

    public Object export(Utterance utt) throws MaryIOException {
        try {

	    def obj = [:]
            def words = utt.getSequence(SupportedSequenceType.WORD);
            obj["tokens"] = words.join(" ")

            def rel = utt.getRelation(SupportedSequenceType.WORD, SupportedSequenceType.PHONE)
            def tmp = [:]
            for (int i=0; i<words.size(); i++) {
                tmp[words[i].toString()] = rel.getRelatedItems(i).join(" ")
            }
            obj["phonetisation"] = tmp

            return obj;
        } catch (Exception ex) {
            throw new MaryIOException("Cannot serialize utt", ex);
        }
    }

    /**
     * Unsupported operation ! We can't import from a TSV formatted input.
     *
     * @param content
     *            unused
     * @return nothing
     * @throws MaryIOException
     *             never done
     */
    public Utterance load(String content) throws MaryIOException {
        throw new UnsupportedOperationException();
    }
}
